import UIKit
import FirebaseCore
import GoogleSignIn

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {

        // 1. Configure Firebase (must be first)
        FirebaseApp.configure()

        // 2. Start the auth bridge (CRITICAL - this connects Kotlin to Swift)
        FirebaseAuthBridge.shared.start()

        print("✅ Firebase and Auth Bridge initialized")

        return true
    }

    // Handle Google Sign-In URL callback (for OAuth redirect)
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Clean up bridge observers when app is closing
        FirebaseAuthBridge.shared.stop()
    }
}