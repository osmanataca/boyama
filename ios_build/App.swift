import SwiftUI

@main
struct AyseninResimAtolyesiApp: App {
    var body: some Scene {
        WindowGroup {
            IosStudioContentView()
        }
    }
}

struct IosPalette {
    let name: String
    let colors: [Color]
}

struct IosArtwork: Identifiable {
    let id: String
    let title: String
    let category: String
    let regionCount: Int
}

struct IosStudioContentView: View {
    private let palettes: [IosPalette] = [
        IosPalette(name: "Kapadokya Gün Batımı", colors: [
            Color(red: 0.91, green: 0.36, blue: 0.29),
            Color(red: 0.96, green: 0.64, blue: 0.38),
            Color(red: 0.91, green: 0.77, blue: 0.42),
            Color(red: 0.16, green: 0.62, blue: 0.56),
            Color(red: 0.15, green: 0.27, blue: 0.33),
            Color(red: 0.61, green: 0.36, blue: 0.90),
            Color(red: 0.95, green: 0.36, blue: 0.71),
            Color(red: 0.97, green: 0.93, blue: 0.89)
        ]),
        IosPalette(name: "Ege Kıyıları", colors: [
            Color(red: 0.00, green: 0.47, blue: 0.71),
            Color(red: 0.00, green: 0.71, blue: 0.85),
            Color(red: 0.56, green: 0.88, blue: 0.94),
            Color(red: 1.00, green: 0.36, blue: 0.56),
            Color(red: 1.00, green: 0.72, blue: 0.01),
            Color(red: 0.98, green: 0.52, blue: 0.00),
            Color(red: 0.01, green: 0.24, blue: 0.54),
            Color(red: 0.79, green: 0.94, blue: 0.97)
        ]),
        IosPalette(name: "Vitray Katedrali", colors: [
            Color(red: 0.85, green: 0.02, blue: 0.16),
            Color(red: 1.00, green: 0.62, blue: 0.11),
            Color(red: 1.00, green: 0.75, blue: 0.41),
            Color(red: 0.18, green: 0.77, blue: 0.71),
            Color(red: 0.23, green: 0.53, blue: 1.00),
            Color(red: 0.51, green: 0.22, blue: 0.93),
            Color(red: 1.00, green: 0.00, blue: 0.43),
            Color(red: 0.11, green: 0.21, blue: 0.34)
        ])
    ]

    private let artworks: [IosArtwork] = [
        IosArtwork(id: "mandala", title: "Anadolu Güneş Mandalası", category: "Mandala & Zen", regionCount: 24),
        IosArtwork(id: "rose", title: "Gotik Gül Vitrayı", category: "Vitray & Mozaik", regionCount: 24),
        IosArtwork(id: "lotus", title: "Zen Nilüfer Göleti", category: "Doğa & Botanik", regionCount: 24)
    ]

    @State private var selectedArtworkIndex: Int = 0
    @State private var selectedPaletteIndex: Int = 0
    @State private var selectedColorIndex: Int = 0
    @State private var showNumbers: Bool = true
    @State private var regionColors: [Int: Color] = [:]

    var body: some View {
        NavigationView {
            VStack(spacing: 14) {
                // Header & Template Selector
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 10) {
                        ForEach(Array(artworks.enumerated()), id: \.element.id) { idx, art ->
                            Button(action: {
                                selectedArtworkIndex = idx
                                regionColors.removeAll()
                            }) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(art.title)
                                        .font(.system(size: 14, weight: .bold, design: .rounded))
                                    Text(art.category)
                                        .font(.system(size: 11))
                                        .opacity(0.8)
                                }
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(selectedArtworkIndex == idx ? Color.orange : Color.gray.opacity(0.18))
                                .foregroundColor(selectedArtworkIndex == idx ? .white : .primary)
                                .cornerRadius(14)
                            }
                        }
                    }
                    .padding(.horizontal)
                }

                // Interactive Mandala / Rose Ring Coloring Grid
                ZStack {
                    RoundedRectangle(cornerRadius: 24)
                        .fill(Color(red: 0.98, green: 0.97, blue: 0.95))
                        .shadow(color: Color.black.opacity(0.08), radius: 8, x: 0, y: 4)

                    GeometryReader { geo in
                        let size = min(geo.size.width, geo.size.height)
                        let center = CGPoint(x: geo.size.width / 2, y: geo.size.height / 2)

                        ZStack {
                            // Outer 12 Petals
                            ForEach(0..<12, id: \.self) { i in
                                let angle = Double(i) * 30.0
                                let regId = i + 1
                                PetalButtonView(
                                    center: center,
                                    radius: size * 0.34,
                                    length: size * 0.22,
                                    angleDegrees: angle,
                                    fillColor: regionColors[regId] ?? Color.white,
                                    numberLabel: showNumbers && regionColors[regId] == nil ? "\((i % 8) + 1)" : nil
                                ) {
                                    regionColors[regId] = palettes[selectedPaletteIndex].colors[selectedColorIndex]
                                }
                            }

                            // Inner 8 Petals
                            ForEach(0..<8, id: \.self) { i in
                                let angle = Double(i) * 45.0 + 15.0
                                let regId = 13 + i
                                PetalButtonView(
                                    center: center,
                                    radius: size * 0.17,
                                    length: size * 0.16,
                                    angleDegrees: angle,
                                    fillColor: regionColors[regId] ?? Color.white,
                                    numberLabel: showNumbers && regionColors[regId] == nil ? "\((i % 8) + 1)" : nil
                                ) {
                                    regionColors[regId] = palettes[selectedPaletteIndex].colors[selectedColorIndex]
                                }
                            }

                            // Center Sun Core
                            Button(action: {
                                regionColors[99] = palettes[selectedPaletteIndex].colors[selectedColorIndex]
                            }) {
                                Circle()
                                    .fill(regionColors[99] ?? Color.white)
                                    .frame(width: size * 0.18, height: size * 0.18)
                                    .overlay(Circle().stroke(Color.black, lineWidth: 2.5))
                                    .overlay(
                                        Text(showNumbers && regionColors[99] == nil ? "1" : "")
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(.black)
                                    )
                            }
                            .position(center)
                        }
                    }
                }
                .padding(.horizontal)

                // Controls Bar
                HStack {
                    Toggle("Sayılarla Boya", isOn: $showNumbers)
                        .font(.system(size: 14, weight: .semibold))

                    Spacer()

                    Button("Palet Değiştir") {
                        selectedPaletteIndex = (selectedPaletteIndex + 1) % palettes.count
                    }
                    .font(.system(size: 13, weight: .bold))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color.blue.opacity(0.15))
                    .cornerRadius(10)

                    Button("Temizle") {
                        regionColors.removeAll()
                    }
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.red)
                    .padding(.horizontal, 10)
                }
                .padding(.horizontal)

                // Color Swatches
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(0..<palettes[selectedPaletteIndex].colors.count, id: \.self) { idx in
                            let c = palettes[selectedPaletteIndex].colors[idx]
                            Button(action: { selectedColorIndex = idx }) {
                                ZStack {
                                    Circle()
                                        .fill(c)
                                        .frame(width: selectedColorIndex == idx ? 50 : 42,
                                               height: selectedColorIndex == idx ? 50 : 42)
                                        .overlay(
                                            Circle()
                                                .stroke(selectedColorIndex == idx ? Color.primary : Color.gray.opacity(0.4),
                                                        lineWidth: selectedColorIndex == idx ? 3 : 1)
                                        )
                                    Text("\(idx + 1)")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(.white)
                                        .shadow(radius: 1)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.bottom, 10)
                }
            }
            .navigationTitle("Ayşe'nin Resim Atölyesi")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct PetalButtonView: View {
    let center: CGPoint
    let radius: CGFloat
    let length: CGFloat
    let angleDegrees: Double
    let fillColor: Color
    let numberLabel: String?
    let onTap: () -> Void

    var body: some View {
        let rad = angleDegrees * .pi / 180.0
        let px = center.x + radius * CGFloat(cos(rad))
        let py = center.y + radius * CGFloat(sin(rad))

        Button(action: onTap) {
            ZStack {
                Capsule()
                    .fill(fillColor)
                    .frame(width: length, height: length * 0.48)
                    .overlay(
                        Capsule().stroke(Color.black, lineWidth: 2)
                    )
                    .rotationEffect(.degrees(angleDegrees))

                if let lbl = numberLabel {
                    Text(lbl)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.black)
                        .padding(4)
                        .background(Circle().fill(Color.white.opacity(0.85)))
                }
            }
        }
        .buttonStyle(PlainButtonStyle())
        .position(x: px, y: py)
    }
}
