import Foundation

@objc public class frp: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
