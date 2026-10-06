import Foundation
import SwiftUI

@main
struct AyseninResimAtolyesiApp: App {
    var body: some Scene {
        WindowGroup {
            IosStudioRootView()
        }
    }
}

struct IosStudioRootView: View {
    @State private var selectedColorIndex: Int = 0
    @State private var regionColors: [Int: Color] = [:]

    private let palette: [Color] = [
        Color(red: 0.91, green: 0.36, blue: 0.29),
        Color(red: 0.96, green: 0.64, blue: 0.38),
        Color(red: 0.91, green: 0.77, blue: 0.42),
        Color(red: 0.16, green: 0.62, blue: 0.56),
        Color(red: 0.00, green: 0.47, blue: 0.71),
        Color(red: 0.61, green: 0.36, blue: 0.90),
        Color(red: 0.95, green: 0.36, blue: 0.71),
        Color(red: 0.20, green: 0.78, blue: 0.45)
    ]

    var body: some View {
        VStack(spacing: 16) {
            Text("Ayşe'nin Resim Atölyesi")
                .font(.system(size: 24, weight: .bold))
                .padding(.top, 16)

            Text("Bölgelere dokunarak mandalayı renklendirin (\(regionColors.count)/13)")
                .font(.system(size: 13))
                .foregroundColor(.secondary)

            ZStack {
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color(red: 0.98, green: 0.97, blue: 0.94))

                // Outer Ring
                Circle()
                    .fill(regionColors[0] ?? Color.white)
                    .frame(width: 260, height: 260)
                    .overlay(Circle().stroke(Color.black, lineWidth: 2))
                    .onTapGesture {
                        regionColors[0] = palette[selectedColorIndex]
                    }

                // 8 Mandala Petals
                ForEach(0..<8, id: \.self) { idx in
                    MandalaPetalItem(
                        index: idx,
                        fillColor: regionColors[idx + 1] ?? Color.white,
                        onSelect: {
                            regionColors[idx + 1] = palette[selectedColorIndex]
                        }
                    )
                }

                // Inner Ring
                Circle()
                    .fill(regionColors[10] ?? Color.white)
                    .frame(width: 110, height: 110)
                    .overlay(Circle().stroke(Color.black, lineWidth: 2))
                    .onTapGesture {
                        regionColors[10] = palette[selectedColorIndex]
                    }

                // Inner Diamond
                Rectangle()
                    .fill(regionColors[11] ?? Color.white)
                    .frame(width: 68, height: 68)
                    .overlay(Rectangle().stroke(Color.black, lineWidth: 2))
                    .rotationEffect(.degrees(45))
                    .onTapGesture {
                        regionColors[11] = palette[selectedColorIndex]
                    }

                // Center Sun Core
                Circle()
                    .fill(regionColors[12] ?? Color.white)
                    .frame(width: 44, height: 44)
                    .overlay(Circle().stroke(Color.black, lineWidth: 2))
                    .onTapGesture {
                        regionColors[12] = palette[selectedColorIndex]
                    }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 320)
            .padding(.horizontal, 20)

            HStack(spacing: 10) {
                ForEach(0..<palette.count, id: \.self) { idx in
                    Circle()
                        .fill(palette[idx])
                        .frame(width: 36, height: 36)
                        .overlay(
                            Circle()
                                .stroke(selectedColorIndex == idx ? Color.black : Color.clear, lineWidth: 3)
                        )
                        .onTapGesture {
                            selectedColorIndex = idx
                        }
                }
            }
            .padding(.horizontal, 16)

            Button(action: {
                regionColors.removeAll()
            }) {
                Text("Tuvali Temizle")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 10)
                    .background(Color.red)
                    .cornerRadius(12)
            }
            .padding(.bottom, 20)
        }
    }
}

struct MandalaPetalItem: View {
    let index: Int
    let fillColor: Color
    let onSelect: () -> Void

    var body: some View {
        Capsule()
            .fill(fillColor)
            .frame(width: 96, height: 42)
            .overlay(Capsule().stroke(Color.black, lineWidth: 2))
            .offset(x: 75, y: 0)
            .rotationEffect(.degrees(Double(index) * 45.0))
            .onTapGesture {
                onSelect()
            }
    }
}
