import Foundation
import UIKit
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import AuthenticationServices

/// Unified Firebase Auth bridge for KMP integration
final class FirebaseAuthBridge {
    static let shared = FirebaseAuthBridge()

    private let center = NotificationCenter.default
    private var authStateHandle: AuthStateDidChangeListenerHandle?
    private var isGoogleFlowActive = false
    private var appleSignInCoordinator: AppleSignInCoordinator?

    private init() {}

    /// Call this from AppDelegate's didFinishLaunchingWithOptions
    func start() {
        // Configure Google Sign-In
        if let clientID = FirebaseApp.app()?.options.clientID {
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        }

        // Listen for auth requests from Kotlin
        center.addObserver(
            self,
            selector: #selector(handleAuthRequest(_:)),
            name: NSNotification.Name("AuthRequest"),
            object: nil
        )

        // Listen for Google Sign-In requests from Kotlin
        center.addObserver(
            self,
            selector: #selector(handleGoogleSignInRequest(_:)),
            name: NSNotification.Name("GoogleSignInRequest"),
            object: nil
        )

        // Listen for Apple Sign-In requests from Kotlin
        center.addObserver(
            self,
            selector: #selector(handleAppleSignInRequest(_:)),
            name: NSNotification.Name("AppleSignInRequest"),
            object: nil
        )

        // Monitor Firebase auth state and broadcast to Kotlin
        authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.broadcastAuthState(user: user)
        }

        // Broadcast initial state
        broadcastAuthState(user: Auth.auth().currentUser)
    }

    func stop() {
        center.removeObserver(self)
        if let handle = authStateHandle {
            Auth.auth().removeStateDidChangeListener(handle)
            authStateHandle = nil
        }
    }

    // MARK: - Auth State Broadcasting

    private func broadcastAuthState(user: User?) {
        let userInfo = buildUserInfo(from: user)
        center.post(
            name: NSNotification.Name("AuthState"),
            object: nil,
            userInfo: userInfo
        )
    }

    // MARK: - Auth Request Handling

    @objc private func handleAuthRequest(_ notification: Notification) {
        guard let info = notification.userInfo as? [String: Any],
              let requestId = info["requestId"] as? String,
              let action = info["action"] as? String else {
            return
        }

        switch action {
        case "anonymous":
            signInAnonymously(requestId: requestId)

        case "google":
            guard let idToken = info["idToken"] as? String, !idToken.isEmpty else {
                postError(requestId: requestId, message: "Missing Google ID token")
                return
            }
            signInWithGoogle(requestId: requestId, idToken: idToken)

        case "apple":
            guard let idToken = info["idToken"] as? String, !idToken.isEmpty else {
                postError(requestId: requestId, message: "Missing Apple ID token")
                return
            }
            signInWithApple(requestId: requestId, idToken: idToken)

        case "facebook":
            guard let accessToken = info["accessToken"] as? String, !accessToken.isEmpty else {
                postError(requestId: requestId, message: "Missing Facebook access token")
                return
            }
            signInWithFacebook(requestId: requestId, accessToken: accessToken)

        case "signUpWithEmailAndPassword":
            guard let email = info["email"] as? String,
                  let password = info["password"] as? String else {
                postError(requestId: requestId, message: "Missing email or password")
                return
            }
            signUpWithEmailAndPassword(requestId: requestId, email: email, password: password)

        case "signInWithEmailAndPassword":
            guard let email = info["email"] as? String,
                  let password = info["password"] as? String else {
                postError(requestId: requestId, message: "Missing email or password")
                return
            }
            signInWithEmailAndPassword(requestId: requestId, email: email, password: password)

        case "sendPasswordResetEmail":
            guard let email = info["email"] as? String else {
                postError(requestId: requestId, message: "Missing email")
                return
            }
            sendPasswordResetEmail(requestId: requestId, email: email)

        case "updatePassword":
            guard let newPassword = info["newPassword"] as? String else {
                postError(requestId: requestId, message: "Missing new password")
                return
            }
            updatePassword(requestId: requestId, newPassword: newPassword)

        case "sendEmailVerification":
            sendEmailVerification(requestId: requestId)

        case "updateProfile":
            let displayName = info["displayName"] as? String
            let photoUrl = info["photoUrl"] as? String
            updateProfile(requestId: requestId, displayName: displayName, photoUrl: photoUrl)

        case "updateEmail":
            guard let newEmail = info["newEmail"] as? String else {
                postError(requestId: requestId, message: "Missing new email")
                return
            }
            updateEmail(requestId: requestId, newEmail: newEmail)

        case "deleteAccount":
            deleteAccount(requestId: requestId)

        case "reloadUser":
            reloadUser(requestId: requestId)

        case "signOut":
            signOut(requestId: requestId)

        default:
            postError(requestId: requestId, message: "Unknown action: \(action)")
        }
    }

    // MARK: - Google Sign-In Flow

    @objc private func handleGoogleSignInRequest(_ notification: Notification) {
        guard !isGoogleFlowActive else {
            print("⚠️ Google sign-in already in progress")
            postGoogleSignInResult(idToken: nil)
            return
        }

        isGoogleFlowActive = true

        // Get presenting view controller
        guard let rootVC = getRootViewController() else {
            print("❌ No root view controller found")
            isGoogleFlowActive = false
            postGoogleSignInResult(idToken: nil)
            return
        }

        // Start Google Sign-In
        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { [weak self] result, error in
            defer { self?.isGoogleFlowActive = false }

            if let error = error {
                print("❌ Google sign-in error: \(error.localizedDescription)")
                self?.postGoogleSignInResult(idToken: nil)
                return
            }

            guard let idToken = result?.user.idToken?.tokenString else {
                print("❌ Failed to get ID token from Google sign-in")
                self?.postGoogleSignInResult(idToken: nil)
                return
            }

            print("✅ Google sign-in successful")
            self?.postGoogleSignInResult(idToken: idToken)
        }
    }

    private func postGoogleSignInResult(idToken: String?) {
        let userInfo: [String: Any] = [
            "idToken": idToken as Any
        ]

        center.post(
            name: NSNotification.Name("GoogleSignInCompleted"),
            object: nil,
            userInfo: userInfo
        )
    }

    // MARK: - Apple Sign-In Flow

    @objc private func handleAppleSignInRequest(_ notification: Notification) {
        if #available(iOS 13.0, *) {
            let appleIDProvider = ASAuthorizationAppleIDProvider()
            let request = appleIDProvider.createRequest()
            request.requestedScopes = [.fullName, .email]

            let authorizationController = ASAuthorizationController(authorizationRequests: [request])

            // Create a coordinator to handle the result
            let coordinator = AppleSignInCoordinator { [weak self] idToken in
                self?.postAppleSignInResult(idToken: idToken)
            }

            authorizationController.delegate = coordinator
            authorizationController.presentationContextProvider = coordinator

            // Store coordinator to prevent deallocation
            self.appleSignInCoordinator = coordinator

            authorizationController.performRequests()
        } else {
            print("❌ Apple Sign-In requires iOS 13.0 or later")
            postAppleSignInResult(idToken: nil)
        }
    }

    private func postAppleSignInResult(idToken: String?) {
        let userInfo: [String: Any] = [
            "idToken": idToken as Any
        ]

        center.post(
            name: NSNotification.Name("AppleSignInCompleted"),
            object: nil,
            userInfo: userInfo
        )
    }

    // MARK: - Firebase Auth Methods

    private func signInAnonymously(requestId: String) {
        Auth.auth().signInAnonymously { [weak self] result, error in
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func signInWithGoogle(requestId: String, idToken: String) {
        let credential = GoogleAuthProvider.credential(
            withIDToken: idToken,
            accessToken: ""
        )

        Auth.auth().signIn(with: credential) { [weak self] result, error in
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func signInWithApple(requestId: String, idToken: String) {
        // Use the appleCredential method which properly handles optional rawNonce
        let credential = OAuthProvider.appleCredential(
            withIDToken: idToken,
            rawNonce: nil,
            fullName: nil
        )

        Auth.auth().signIn(with: credential) { [weak self] result, error in
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func signInWithFacebook(requestId: String, accessToken: String) {
        let credential = FacebookAuthProvider.credential(
            withAccessToken: accessToken
        )

        Auth.auth().signIn(with: credential) { [weak self] result, error in
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func signUpWithEmailAndPassword(requestId: String, email: String, password: String) {
        print("🔐 Sign up with email: \(email)")
        Auth.auth().createUser(withEmail: email, password: password) { [weak self] result, error in
            if let error = error {
                print("❌ Sign up failed: \(error.localizedDescription)")
            } else {
                print("✅ Sign up successful: \(email)")
            }
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func signInWithEmailAndPassword(requestId: String, email: String, password: String) {
        print("🔐 Sign in with email: \(email)")
        Auth.auth().signIn(withEmail: email, password: password) { [weak self] result, error in
            if let error = error {
                print("❌ Sign in failed: \(error.localizedDescription)")
            } else {
                print("✅ Sign in successful: \(email)")
            }
            self?.postAuthResponse(
                requestId: requestId,
                result: result,
                error: error
            )
        }
    }

    private func sendPasswordResetEmail(requestId: String, email: String) {
        Auth.auth().sendPasswordReset(withEmail: email) { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func updatePassword(requestId: String, newPassword: String) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        user.updatePassword(to: newPassword) { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func sendEmailVerification(requestId: String) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        user.sendEmailVerification { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func updateProfile(requestId: String, displayName: String?, photoUrl: String?) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        let changeRequest = user.createProfileChangeRequest()
        if let displayName = displayName {
            changeRequest.displayName = displayName
        }
        if let photoUrl = photoUrl, let url = URL(string: photoUrl) {
            changeRequest.photoURL = url
        }

        changeRequest.commitChanges { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func updateEmail(requestId: String, newEmail: String) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        user.updateEmail(to: newEmail) { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func deleteAccount(requestId: String) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        user.delete { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func reloadUser(requestId: String) {
        guard let user = Auth.auth().currentUser else {
            postError(requestId: requestId, message: "No authenticated user")
            return
        }

        user.reload { [weak self] error in
            self?.postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    private func signOut(requestId: String) {
        do {
            try Auth.auth().signOut()
            // Also sign out from Google
            GIDSignIn.sharedInstance.signOut()

            postAuthResponse(
                requestId: requestId,
                result: nil,
                error: nil
            )
        } catch {
            postAuthResponse(
                requestId: requestId,
                result: nil,
                error: error
            )
        }
    }

    // MARK: - Response Helpers

    private func postAuthResponse(
        requestId: String,
        result: AuthDataResult?,
        error: Error?
    ) {
        var userInfo: [String: Any] = ["requestId": requestId]

        if let error = error as NSError? {
            userInfo["status"] = "failure"
            userInfo["errorCode"] = "\(error.domain):\(error.code)"
            userInfo["errorMessage"] = error.localizedDescription
        } else {
            userInfo["status"] = "success"
            let user = result?.user ?? Auth.auth().currentUser
            userInfo.merge(buildUserInfo(from: user)) { _, new in new }
        }

        center.post(
            name: NSNotification.Name("AuthResponse"),
            object: nil,
            userInfo: userInfo
        )
    }

    private func postError(requestId: String, message: String) {
        let userInfo: [String: Any] = [
            "requestId": requestId,
            "status": "failure",
            "errorMessage": message
        ]

        center.post(
            name: NSNotification.Name("AuthResponse"),
            object: nil,
            userInfo: userInfo
        )
    }

    private func buildUserInfo(from user: User?) -> [String: Any] {
        guard let user = user else {
            return ["uid": NSNull()]
        }

        var info: [String: Any] = [
            "uid": user.uid,
            "isAnonymous": user.isAnonymous,
            "isEmailVerified": user.isEmailVerified
        ]

        if let displayName = user.displayName {
            info["displayName"] = displayName
        }
        if let email = user.email {
            info["email"] = email
        }
        if let photoURL = user.photoURL?.absoluteString {
            info["photoUrl"] = photoURL
        }

        // Add provider data
        let providerIds = user.providerData.map { $0.providerID }
        info["providerData"] = providerIds

        return info
    }

    // MARK: - Helpers

    private func getRootViewController() -> UIViewController? {
        return UIApplication.shared
        .connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow }?
        .rootViewController
    }
}

// MARK: - Apple Sign-In Coordinator

@available(iOS 13.0, *)
private class AppleSignInCoordinator: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    private let completion: (String?) -> Void

    init(completion: @escaping (String?) -> Void) {
        self.completion = completion
        super.init()
    }

    // MARK: - ASAuthorizationControllerDelegate

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let identityTokenData = appleIDCredential.identityToken,
              let idToken = String(data: identityTokenData, encoding: .utf8) else {
            print("❌ Failed to get identity token from Apple Sign-In")
            completion(nil)
            return
        }

        print("✅ Apple sign-in successful")
        completion(idToken)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        print("❌ Apple sign-in error: \(error.localizedDescription)")
        completion(nil)
    }

    // MARK: - ASAuthorizationControllerPresentationContextProviding

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        return UIApplication.shared
        .connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow } ?? UIWindow()
    }
}