import UIKit
import SwiftUI
import ComposeApp
import GoogleSignIn


struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
            .onOpenURL { url in
                // Handle Google Sign-In URL callbacks
                GIDSignIn.sharedInstance.handle(url)
            }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // This calls your Kotlin MainViewController() function
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op
    }
}


