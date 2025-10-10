import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    // Attach UIKit AppDelegate to satisfy SDK swizzlers and ensure early Firebase config
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
