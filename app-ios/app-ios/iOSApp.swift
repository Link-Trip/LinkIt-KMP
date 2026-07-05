import GoogleMaps
import SwiftUI

@main
struct iOSApp: App {
    @State private var showIntro = true

    init() {
        if let apiKey = Bundle.main.object(forInfoDictionaryKey: "GoogleMapsApiKey") as? String,
           !apiKey.isEmpty {
            GMSServices.provideAPIKey(apiKey)
        }
    }

    var body: some Scene {
        WindowGroup {
            if showIntro {
                IntroComposeView(onComplete: {
                    showIntro = false
                })
                .ignoresSafeArea()
            } else {
                ContentView()
            }
        }
    }
}
