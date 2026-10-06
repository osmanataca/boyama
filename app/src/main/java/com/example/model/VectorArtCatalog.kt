package com.example.model

import androidx.compose.ui.geometry.Offset
import com.example.model.VectorGeometryBuilder.circleShape
import com.example.model.VectorGeometryBuilder.ovalShape
import com.example.model.VectorGeometryBuilder.petalShape
import com.example.model.VectorGeometryBuilder.polarOffset
import com.example.model.VectorGeometryBuilder.polygonShape
import com.example.model.VectorGeometryBuilder.regularPolygonShape
import com.example.model.VectorGeometryBuilder.ringSectorShape
import com.example.model.VectorGeometryBuilder.roundedRectShape
import com.example.model.VectorGeometryBuilder.starShape
import com.example.model.VectorGeometryBuilder.waveBandShape

object VectorArtCatalog {

    val allTemplates: List<ArtworkTemplate> by lazy {
        listOf(
            buildAnatolianMandala(),
            buildNightOwl(),
            buildStainedRoseWindow(),
            buildCappadociaBalloons(),
            buildAegeanLighthouse(),
            buildCosmicGalaxy(),
            buildBotanicalMonstera(),
            buildRoyalPeacock(),
            buildZenLotusKoi(),
            buildCrystalDragonMedallion()
        )
    }

    fun findById(id: String): ArtworkTemplate {
        return allTemplates.find { it.id == id } ?: allTemplates.first()
    }

    private fun makeRegion(
        id: Int,
        nameTr: String,
        targetColorIndex: Int,
        labelCenter: Offset,
        shape: VectorShape
    ): ColorRegion {
        return ColorRegion(
            id = id,
            nameTr = nameTr,
            targetColorIndex = targetColorIndex,
            labelCenter = labelCenter,
            path = shape.path,
            hitPolygon = shape.hitPolygon
        )
    }

    // 1. Anadolu Güneş Mandalası (29 Regions)
    private fun buildAnatolianMandala(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var nextId = 1

        regions.add(
            makeRegion(
                id = nextId++,
                nameTr = "Kadim Çerçeve",
                targetColorIndex = 7,
                labelCenter = Offset(115f, 115f),
                shape = roundedRectShape(40f, 40f, 960f, 960f, 90f)
            )
        )

        val corners = listOf(
            Offset(145f, 145f),
            Offset(855f, 145f),
            Offset(855f, 855f),
            Offset(145f, 855f)
        )
        corners.forEachIndexed { idx, c ->
            regions.add(
                makeRegion(
                    id = nextId++,
                    nameTr = "Köşe Rozeti ${idx + 1}",
                    targetColorIndex = 3,
                    labelCenter = c,
                    shape = starShape(c.x, c.y, 70f, 36f, 8)
                )
            )
        }

        regions.add(
            makeRegion(
                id = nextId++,
                nameTr = "Dış Hale Halkası",
                targetColorIndex = 4,
                labelCenter = polarOffset(500f, 500f, 395f, -90f),
                shape = circleShape(500f, 500f, 425f)
            )
        )

        for (i in 0 until 8) {
            val angle = i * 45f
            regions.add(
                makeRegion(
                    id = nextId++,
                    nameTr = "Dış Lotus Yaprağı ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 0 else 1,
                    labelCenter = polarOffset(500f, 500f, 320f, angle),
                    shape = petalShape(500f, 500f, 210f, 405f, angle, 21f)
                )
            )
        }

        for (i in 0 until 8) {
            val angle = i * 45f + 22.5f
            regions.add(
                makeRegion(
                    id = nextId++,
                    nameTr = "Geometrik Taç ${i + 1}",
                    targetColorIndex = 2,
                    labelCenter = polarOffset(500f, 500f, 290f, angle),
                    shape = petalShape(500f, 500f, 195f, 355f, angle, 14f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = nextId++,
                nameTr = "Orta Kalkan Halkası",
                targetColorIndex = 3,
                labelCenter = polarOffset(500f, 500f, 195f, -45f),
                shape = circleShape(500f, 500f, 220f)
            )
        )

        for (i in 0 until 6) {
            val angle = i * 60f
            regions.add(
                makeRegion(
                    id = nextId++,
                    nameTr = "İç Güneş Yaprağı ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 5 else 6,
                    labelCenter = polarOffset(500f, 500f, 142f, angle),
                    shape = petalShape(500f, 500f, 70f, 195f, angle, 26f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = nextId++,
                nameTr = "Merkez Selçuklu Yıldızı",
                targetColorIndex = 2,
                labelCenter = Offset(500f, 435f),
                shape = starShape(500f, 500f, 96f, 52f, 8)
            )
        )

        regions.add(
            makeRegion(
                id = nextId++,
                nameTr = "Güneş Çekirdeği",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 500f),
                shape = circleShape(500f, 500f, 40f)
            )
        )

        return ArtworkTemplate(
            id = "anatolian_mandala",
            titleTr = "Anadolu Güneş Mandalası",
            artistNoteTr = "Selçuklu yıldız geometrisi ve lotus yapraklarından ilham alan meditatif mandala.",
            category = ArtCategory.MANDALA,
            difficulty = DifficultyLevel.MEDIUM,
            defaultPaletteId = "cappadocia_sunset",
            isDailyFeatured = true,
            regions = regions
        )
    }

    // 2. Gizemli Gece Baykuşu (24 Regions)
    private fun buildNightOwl(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Gece Gökyüzü",
                targetColorIndex = 7,
                labelCenter = Offset(150f, 150f),
                shape = roundedRectShape(45f, 45f, 955f, 955f, 80f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Dolunay Halesi",
                targetColorIndex = 5,
                labelCenter = Offset(745f, 215f),
                shape = circleShape(745f, 215f, 125f)
            )
        )

        val starCenters = listOf(Offset(210f, 210f), Offset(320f, 130f), Offset(165f, 380f))
        starCenters.forEachIndexed { idx, sc ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Kutup Yıldızı ${idx + 1}",
                    targetColorIndex = 5,
                    labelCenter = sc,
                    shape = starShape(sc.x, sc.y, 44f, 20f, 5)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kuyruk Tüyleri",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 875f),
                shape = polygonShape(
                    listOf(
                        410f to 760f,
                        590f to 760f,
                        635f to 920f,
                        500f to 895f,
                        365f to 920f
                    )
                )
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Baykuş Gövdesi",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 690f),
                shape = ovalShape(500f, 600f, 225f, 230f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Kanat",
                targetColorIndex = 0,
                labelCenter = Offset(285f, 595f),
                shape = ovalShape(300f, 585f, 68f, 165f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Kanat",
                targetColorIndex = 0,
                labelCenter = Offset(715f, 595f),
                shape = ovalShape(700f, 585f, 68f, 165f)
            )
        )

        val chestCenters = listOf(Offset(445f, 555f), Offset(555f, 555f), Offset(500f, 635f))
        chestCenters.forEachIndexed { idx, cc ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Göğüs Tüyü ${idx + 1}",
                    targetColorIndex = 2,
                    labelCenter = cc,
                    shape = petalShape(cc.x, cc.y - 45f, 5f, 92f, 90f, 38f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Baykuş Başlığı",
                targetColorIndex = 4,
                labelCenter = Offset(500f, 260f),
                shape = ovalShape(500f, 345f, 195f, 155f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Kulak Sorgucu",
                targetColorIndex = 3,
                labelCenter = Offset(340f, 200f),
                shape = polygonShape(listOf(310f to 275f, 290f to 145f, 415f to 225f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Kulak Sorgucu",
                targetColorIndex = 3,
                labelCenter = Offset(660f, 200f),
                shape = polygonShape(listOf(690f to 275f, 710f to 145f, 585f to 225f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Göz Halesi",
                targetColorIndex = 5,
                labelCenter = Offset(390f, 305f),
                shape = circleShape(415f, 350f, 76f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Göz Halesi",
                targetColorIndex = 5,
                labelCenter = Offset(610f, 305f),
                shape = circleShape(585f, 350f, 76f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol İris",
                targetColorIndex = 2,
                labelCenter = Offset(415f, 350f),
                shape = circleShape(415f, 350f, 42f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ İris",
                targetColorIndex = 2,
                labelCenter = Offset(585f, 350f),
                shape = circleShape(585f, 350f, 42f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Elmas Gaga",
                targetColorIndex = 5,
                labelCenter = Offset(500f, 415f),
                shape = polygonShape(listOf(500f to 365f, 540f to 415f, 500f to 470f, 460f to 415f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Alın Kristali",
                targetColorIndex = 6,
                labelCenter = Offset(500f, 245f),
                shape = polygonShape(listOf(500f to 200f, 535f to 245f, 500f to 290f, 465f to 245f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Meşe Dalı",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 795f),
                shape = roundedRectShape(120f, 770f, 880f, 825f, 26f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Yaprak",
                targetColorIndex = 6,
                labelCenter = Offset(175f, 725f),
                shape = petalShape(220f, 780f, 10f, 115f, -135f, 28f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Yaprak",
                targetColorIndex = 6,
                labelCenter = Offset(825f, 725f),
                shape = petalShape(780f, 780f, 10f, 115f, -45f, 28f)
            )
        )

        return ArtworkTemplate(
            id = "mystic_night_owl",
            titleTr = "Gizemli Gece Baykuşu",
            artistNoteTr = "Dolunay ışığında meşe dalında nöbet tutan bilge orman baykuşu.",
            category = ArtCategory.ANIMALS,
            difficulty = DifficultyLevel.EASY,
            defaultPaletteId = "cosmic_nebula",
            regions = regions
        )
    }

    // 3. Gotik Gül Vitrayı (26 Regions)
    private fun buildStainedRoseWindow(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Katedral Kemeri",
                targetColorIndex = 7,
                labelCenter = Offset(500f, 105f),
                shape = roundedRectShape(65f, 55f, 935f, 945f, 180f)
            )
        )

        for (i in 0 until 8) {
            val startAngle = i * 45f
            val midAngle = startAngle + 22.5f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Dış Vitray Kemeri ${i + 1}",
                    targetColorIndex = i % 6,
                    labelCenter = polarOffset(500f, 520f, 330f, midAngle),
                    shape = ringSectorShape(500f, 520f, 250f, 390f, startAngle, 45f)
                )
            )
        }

        for (i in 0 until 8) {
            val startAngle = i * 45f + 22.5f
            val midAngle = startAngle + 22.5f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Orta Vitray Dilimi ${i + 1}",
                    targetColorIndex = (i + 2) % 7,
                    labelCenter = polarOffset(500f, 520f, 185f, midAngle),
                    shape = ringSectorShape(500f, 520f, 120f, 250f, startAngle, 45f)
                )
            )
        }

        for (i in 0 until 8) {
            val angle = i * 45f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Gül Vitray Yaprağı ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 0 else 6,
                    labelCenter = polarOffset(500f, 520f, 92f, angle),
                    shape = petalShape(500f, 520f, 32f, 138f, angle, 20f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Merkez Yakut Taşı",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 520f),
                shape = circleShape(500f, 520f, 44f)
            )
        )

        return ArtworkTemplate(
            id = "stained_rose_window",
            titleTr = "Gotik Gül Vitrayı",
            artistNoteTr = "Işığı mücevher tonlarında kıran simetrik katedral gül penceresi.",
            category = ArtCategory.STAINED_GLASS,
            difficulty = DifficultyLevel.EXPERT,
            defaultPaletteId = "stained_cathedral",
            regions = regions
        )
    }

    // 4. Kapadokya Balonları (19 Regions)
    private fun buildCappadociaBalloons(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sabah Gökyüzü",
                targetColorIndex = 7,
                labelCenter = Offset(200f, 130f),
                shape = roundedRectShape(45f, 45f, 955f, 955f, 70f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Doğan Güneş",
                targetColorIndex = 2,
                labelCenter = Offset(780f, 220f),
                shape = circleShape(780f, 220f, 110f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kızılçukur Tepeleri",
                targetColorIndex = 1,
                labelCenter = Offset(240f, 780f),
                shape = waveBandShape(45f, 955f, 735f, 955f, 42f, 2, 0.5f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Vadi Tabanı",
                targetColorIndex = 4,
                labelCenter = Offset(500f, 900f),
                shape = waveBandShape(45f, 955f, 835f, 955f, 28f, 3, 2.0f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Peri Bacası",
                targetColorIndex = 7,
                labelCenter = Offset(185f, 810f),
                shape = polygonShape(listOf(140f to 890f, 165f to 700f, 205f to 700f, 230f to 890f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Peri Bacası Şapkası",
                targetColorIndex = 0,
                labelCenter = Offset(185f, 685f),
                shape = polygonShape(listOf(145f to 705f, 185f to 650f, 225f to 705f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Peri Bacası",
                targetColorIndex = 7,
                labelCenter = Offset(825f, 800f),
                shape = polygonShape(listOf(780f to 890f, 805f to 680f, 845f to 680f, 870f to 890f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Baca Şapkası",
                targetColorIndex = 0,
                labelCenter = Offset(825f, 665f),
                shape = polygonShape(listOf(785f to 685f, 825f to 630f, 865f to 685f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Uzak Sol Balon",
                targetColorIndex = 5,
                labelCenter = Offset(185f, 390f),
                shape = ovalShape(185f, 390f, 68f, 82f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Uzak Sağ Balon",
                targetColorIndex = 6,
                labelCenter = Offset(815f, 470f),
                shape = ovalShape(815f, 470f, 64f, 78f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Ana Balon Kubbesi",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 175f),
                shape = ovalShape(500f, 360f, 220f, 240f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Üst Kemer Kuşağı",
                targetColorIndex = 2,
                labelCenter = Offset(500f, 265f),
                shape = roundedRectShape(315f, 230f, 685f, 300f, 30f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Orta Turkuaz Kuşak",
                targetColorIndex = 3,
                labelCenter = Offset(500f, 355f),
                shape = roundedRectShape(295f, 315f, 705f, 395f, 35f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Alt Eflatun Kuşak",
                targetColorIndex = 5,
                labelCenter = Offset(500f, 445f),
                shape = roundedRectShape(320f, 410f, 680f, 480f, 30f)
            )
        )

        val diamondXs = listOf(380f, 500f, 620f)
        diamondXs.forEachIndexed { idx, dx ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Balon Motifi ${idx + 1}",
                    targetColorIndex = 6,
                    labelCenter = Offset(dx, 355f),
                    shape = polygonShape(listOf(dx to 320f, dx + 38f to 355f, dx to 390f, dx - 38f to 355f))
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Balon Boğazı",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 565f),
                shape = polygonShape(listOf(415f to 530f, 585f to 530f, 545f to 605f, 455f to 605f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Hasır Sepet",
                targetColorIndex = 4,
                labelCenter = Offset(500f, 685f),
                shape = roundedRectShape(445f, 640f, 555f, 730f, 18f)
            )
        )

        return ArtworkTemplate(
            id = "cappadocia_balloons",
            titleTr = "Kapadokya Balonları",
            artistNoteTr = "Peri bacaları üzerinde gün doğumunu selamlayan rengarenk sıcak hava balonları.",
            category = ArtCategory.STAINED_GLASS,
            difficulty = DifficultyLevel.MEDIUM,
            defaultPaletteId = "cappadocia_sunset",
            regions = regions
        )
    }

    // 5. Ege Deniz Feneri & Dalgalar (18 Regions)
    private fun buildAegeanLighthouse(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Ege Gökyüzü",
                targetColorIndex = 7,
                labelCenter = Offset(220f, 140f),
                shape = roundedRectShape(45f, 45f, 955f, 955f, 75f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Ufuk Güneşi",
                targetColorIndex = 4,
                labelCenter = Offset(720f, 360f),
                shape = circleShape(720f, 360f, 135f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Üst Işık Hüzmesi",
                targetColorIndex = 5,
                labelCenter = Offset(660f, 195f),
                shape = polygonShape(listOf(380f to 235f, 920f to 120f, 920f to 240f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Alt Işık Hüzmesi",
                targetColorIndex = 4,
                labelCenter = Offset(160f, 210f),
                shape = polygonShape(listOf(300f to 235f, 70f to 140f, 70f to 265f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Kıyı Bulutu",
                targetColorIndex = 2,
                labelCenter = Offset(175f, 415f),
                shape = ovalShape(175f, 415f, 78f, 38f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Ufuk Bulutu",
                targetColorIndex = 2,
                labelCenter = Offset(555f, 320f),
                shape = ovalShape(555f, 320f, 72f, 34f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Körfez Adası",
                targetColorIndex = 6,
                labelCenter = Offset(620f, 650f),
                shape = ovalShape(620f, 660f, 95f, 36f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Yelkenli Kanadı",
                targetColorIndex = 3,
                labelCenter = Offset(765f, 535f),
                shape = polygonShape(listOf(760f to 450f, 825f to 580f, 715f to 580f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Yelkenli Gövdesi",
                targetColorIndex = 6,
                labelCenter = Offset(770f, 605f),
                shape = polygonShape(listOf(700f to 585f, 840f to 585f, 815f to 630f, 725f to 630f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Fener Kubbesi",
                targetColorIndex = 3,
                labelCenter = Offset(340f, 175f),
                shape = polygonShape(listOf(280f to 205f, 340f to 130f, 400f to 205f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Fener Lambası",
                targetColorIndex = 4,
                labelCenter = Offset(340f, 240f),
                shape = roundedRectShape(290f, 205f, 390f, 275f, 12f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Üst Kule Kuşağı",
                targetColorIndex = 3,
                labelCenter = Offset(340f, 335f),
                shape = polygonShape(listOf(280f to 275f, 400f to 275f, 415f to 395f, 265f to 395f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Orta Beyaz Kuşak",
                targetColorIndex = 7,
                labelCenter = Offset(340f, 455f),
                shape = polygonShape(listOf(265f to 395f, 415f to 395f, 430f to 515f, 250f to 515f))
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Alt Kule Kuşağı",
                targetColorIndex = 3,
                labelCenter = Offset(340f, 585f),
                shape = polygonShape(listOf(250f to 515f, 430f to 515f, 445f to 665f, 235f to 665f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kayalık Burun",
                targetColorIndex = 5,
                labelCenter = Offset(335f, 705f),
                shape = polygonShape(listOf(140f to 765f, 215f to 650f, 465f to 650f, 540f to 765f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Açık Deniz Dalgası",
                targetColorIndex = 2,
                labelCenter = Offset(680f, 725f),
                shape = waveBandShape(45f, 955f, 685f, 955f, 30f, 3, 0f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Orta Kıyı Dalgası",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 815f),
                shape = waveBandShape(45f, 955f, 775f, 955f, 34f, 3, 1.6f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Derin Deniz Dalgası",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 905f),
                shape = waveBandShape(45f, 955f, 865f, 955f, 26f, 4, 3.1f)
            )
        )

        return ArtworkTemplate(
            id = "aegean_lighthouse",
            titleTr = "Ege Deniz Feneri",
            artistNoteTr = "Köpüklü Ege dalgaları ve gün batımında yol gösteren kıyı feneri.",
            category = ArtCategory.NATURE,
            difficulty = DifficultyLevel.EASY,
            defaultPaletteId = "aegean_breeze",
            regions = regions
        )
    }

    // 6. Kozmik Galaksi & Gezegenler (18 Regions)
    private fun buildCosmicGalaxy(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Derin Uzay Boşluğu",
                targetColorIndex = 7,
                labelCenter = Offset(150f, 150f),
                shape = roundedRectShape(45f, 45f, 955f, 955f, 80f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Nebula Bulutsusu",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 220f),
                shape = ovalShape(500f, 480f, 370f, 330f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Dış Satürn Halkası",
                targetColorIndex = 2,
                labelCenter = Offset(205f, 520f),
                shape = ovalShape(500f, 520f, 360f, 110f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "İç Satürn Halkası",
                targetColorIndex = 5,
                labelCenter = Offset(785f, 520f),
                shape = ovalShape(500f, 520f, 290f, 75f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Halkalı Gezegen",
                targetColorIndex = 3,
                labelCenter = Offset(500f, 430f),
                shape = circleShape(500f, 520f, 165f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Gezegen Kuşağı",
                targetColorIndex = 4,
                labelCenter = Offset(500f, 520f),
                shape = roundedRectShape(340f, 485f, 660f, 555f, 35f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Buzul Uydusu",
                targetColorIndex = 6,
                labelCenter = Offset(750f, 225f),
                shape = circleShape(750f, 225f, 78f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kızıl Uydu",
                targetColorIndex = 5,
                labelCenter = Offset(240f, 770f),
                shape = circleShape(240f, 770f, 85f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kuyruklu Yıldız İzi",
                targetColorIndex = 2,
                labelCenter = Offset(280f, 260f),
                shape = polygonShape(listOf(130f to 160f, 385f to 250f, 345f to 315f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kuyruklu Yıldız Çekirdeği",
                targetColorIndex = 5,
                labelCenter = Offset(375f, 285f),
                shape = starShape(375f, 285f, 52f, 24f, 6)
            )
        )

        val stars = listOf(
            Offset(740f, 770f),
            Offset(550f, 830f),
            Offset(830f, 390f),
            Offset(165f, 360f),
            Offset(230f, 590f),
            Offset(820f, 650f),
            Offset(480f, 165f),
            Offset(630f, 290f)
        )
        stars.forEachIndexed { idx, s ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Galaksi Yıldızı ${idx + 1}",
                    targetColorIndex = if (idx % 2 == 0) 5 else 2,
                    labelCenter = s,
                    shape = starShape(s.x, s.y, 44f, 20f, 4)
                )
            )
        }

        return ArtworkTemplate(
            id = "cosmic_galaxy",
            titleTr = "Kozmik Galaksi & Halkalar",
            artistNoteTr = "Kuyruklu yıldızlar, uydular ve parıldayan nebula bulutları arasında Satürn.",
            category = ArtCategory.COSMIC,
            difficulty = DifficultyLevel.EASY,
            defaultPaletteId = "cosmic_nebula",
            regions = regions
        )
    }

    // 7. Tropikal Monstera & Orkide (17 Regions)
    private fun buildBotanicalMonstera(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sera Kemeri",
                targetColorIndex = 3,
                labelCenter = Offset(500f, 120f),
                shape = roundedRectShape(65f, 55f, 935f, 945f, 160f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Altın Sera Güneşi",
                targetColorIndex = 6,
                labelCenter = Offset(500f, 250f),
                shape = circleShape(500f, 380f, 235f)
            )
        )

        val leafAngles = listOf(-145f, -115f, -65f, -35f, 165f, 15f)
        leafAngles.forEachIndexed { idx, ang ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Tropikal Yaprak ${idx + 1}",
                    targetColorIndex = if (idx % 2 == 0) 0 else 1,
                    labelCenter = polarOffset(500f, 560f, 275f, ang),
                    shape = petalShape(500f, 580f, 70f, 380f, ang, 22f)
                )
            )
        }

        for (i in 0 until 5) {
            val ang = -90f + i * 72f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Orkide Taç Yaprağı ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 4 else 5,
                    labelCenter = polarOffset(500f, 450f, 115f, ang),
                    shape = petalShape(500f, 450f, 28f, 175f, ang, 30f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Orkide Özü",
                targetColorIndex = 6,
                labelCenter = Offset(500f, 450f),
                shape = circleShape(500f, 450f, 42f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Terracotta Vazo",
                targetColorIndex = 7,
                labelCenter = Offset(500f, 780f),
                shape = ovalShape(500f, 770f, 155f, 135f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Vazo Boğazı",
                targetColorIndex = 6,
                labelCenter = Offset(500f, 640f),
                shape = roundedRectShape(390f, 615f, 610f, 665f, 22f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Vazo Nakışı",
                targetColorIndex = 2,
                labelCenter = Offset(500f, 770f),
                shape = roundedRectShape(360f, 740f, 640f, 800f, 24f)
            )
        )

        return ArtworkTemplate(
            id = "botanical_monstera",
            titleTr = "Tropikal Monstera & Orkide",
            artistNoteTr = "El yapımı terracotta vazoda egzotik orkide ve monstera yaprakları.",
            category = ArtCategory.NATURE,
            difficulty = DifficultyLevel.MEDIUM,
            defaultPaletteId = "botanical_garden",
            regions = regions
        )
    }

    // 8. Kraliyet Tavus Kuşu (25 Regions)
    private fun buildRoyalPeacock(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Saray Bahçesi",
                targetColorIndex = 7,
                labelCenter = Offset(140f, 140f),
                shape = roundedRectShape(45f, 45f, 955f, 955f, 85f)
            )
        )

        for (i in 0 until 9) {
            val angle = -170f + i * 20f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Tavus Tüyü ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 3 else 4,
                    labelCenter = polarOffset(500f, 700f, 340f, angle),
                    shape = petalShape(500f, 700f, 95f, 440f, angle, 11f)
                )
            )
        }

        for (i in 0 until 9) {
            val angle = -170f + i * 20f
            val spotCenter = polarOffset(500f, 700f, 365f, angle)
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Tüy Gözü ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 1 else 5,
                    labelCenter = spotCenter,
                    shape = circleShape(spotCenter.x, spotCenter.y, 28f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Kraliyet Gövdesi",
                targetColorIndex = 4,
                labelCenter = Offset(500f, 675f),
                shape = ovalShape(500f, 680f, 95f, 155f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Tavus Başlığı",
                targetColorIndex = 5,
                labelCenter = Offset(500f, 495f),
                shape = circleShape(500f, 495f, 58f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Altın Sorguç",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 405f),
                shape = polygonShape(listOf(465f to 440f, 500f to 365f, 535f to 440f))
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sol Kanat",
                targetColorIndex = 6,
                labelCenter = Offset(425f, 685f),
                shape = ovalShape(425f, 685f, 42f, 95f)
            )
        )
        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sağ Kanat",
                targetColorIndex = 6,
                labelCenter = Offset(575f, 685f),
                shape = ovalShape(575f, 685f, 42f, 95f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Mermer Kaide",
                targetColorIndex = 2,
                labelCenter = Offset(500f, 875f),
                shape = roundedRectShape(320f, 835f, 680f, 915f, 28f)
            )
        )

        return ArtworkTemplate(
            id = "royal_peacock",
            titleTr = "Kraliyet Tavus Kuşu",
            artistNoteTr = "Yelpaze gibi açılan dokuz gözlü görkemli kuyruğuyla saray tavus kuşu.",
            category = ArtCategory.ANIMALS,
            difficulty = DifficultyLevel.EXPERT,
            defaultPaletteId = "stained_cathedral",
            regions = regions
        )
    }

    // 9. Zen Lotus Göleti & Koi (20 Regions)
    private fun buildZenLotusKoi(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Gölet Taş Kenarı",
                targetColorIndex = 7,
                labelCenter = Offset(500f, 95f),
                shape = circleShape(500f, 500f, 445f)
            )
        )

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Dingin Su Yüzeyi",
                targetColorIndex = 0,
                labelCenter = Offset(500f, 175f),
                shape = circleShape(500f, 500f, 385f)
            )
        )

        val padCenters = listOf(
            Offset(250f, 340f),
            Offset(750f, 340f),
            Offset(500f, 770f)
        )
        padCenters.forEachIndexed { idx, pc ->
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Nilüfer Yaprağı ${idx + 1}",
                    targetColorIndex = 2,
                    labelCenter = pc,
                    shape = circleShape(pc.x, pc.y, 82f)
                )
            )
        }

        for (i in 0 until 8) {
            val angle = i * 45f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Dış Nilüfer Taç Yaprağı ${i + 1}",
                    targetColorIndex = 3,
                    labelCenter = polarOffset(500f, 470f, 175f, angle),
                    shape = petalShape(500f, 470f, 55f, 235f, angle, 22f)
                )
            )
        }

        for (i in 0 until 6) {
            val angle = i * 60f + 15f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "İç Nilüfer Yaprağı ${i + 1}",
                    targetColorIndex = 4,
                    labelCenter = polarOffset(500f, 470f, 98f, angle),
                    shape = petalShape(500f, 470f, 25f, 140f, angle, 24f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Nilüfer Tohum Kalbi",
                targetColorIndex = 5,
                labelCenter = Offset(500f, 470f),
                shape = circleShape(500f, 470f, 45f)
            )
        )

        return ArtworkTemplate(
            id = "zen_lotus_koi",
            titleTr = "Zen Nilüfer Göleti",
            artistNoteTr = "Berrak gölet sularında açan katmanlı nilüfer çiçeği ve su yaprakları.",
            category = ArtCategory.MANDALA,
            difficulty = DifficultyLevel.EASY,
            defaultPaletteId = "aegean_breeze",
            regions = regions
        )
    }

    // 10. Kristal Ejderha Madalyonu (17 Regions)
    private fun buildCrystalDragonMedallion(): ArtworkTemplate {
        val regions = mutableListOf<ColorRegion>()
        var id = 1

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Sekizgen Kalkan",
                targetColorIndex = 7,
                labelCenter = Offset(500f, 105f),
                shape = regularPolygonShape(500f, 500f, 445f, 8, 22.5f)
            )
        )

        for (i in 0 until 8) {
            val angle = i * 45f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Kristal Işın ${i + 1}",
                    targetColorIndex = if (i % 2 == 0) 2 else 3,
                    labelCenter = polarOffset(500f, 500f, 285f, angle),
                    shape = petalShape(500f, 500f, 140f, 385f, angle, 18f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Altıgen Zırh",
                targetColorIndex = 1,
                labelCenter = Offset(500f, 340f),
                shape = regularPolygonShape(500f, 500f, 195f, 6, 0f)
            )
        )

        for (i in 0 until 6) {
            val angle = i * 60f
            regions.add(
                makeRegion(
                    id = id++,
                    nameTr = "Element Kristali ${i + 1}",
                    targetColorIndex = (i % 5) + 1,
                    labelCenter = polarOffset(500f, 500f, 110f, angle),
                    shape = petalShape(500f, 500f, 35f, 165f, angle, 24f)
                )
            )
        }

        regions.add(
            makeRegion(
                id = id++,
                nameTr = "Ejderha Kalbi",
                targetColorIndex = 5,
                labelCenter = Offset(500f, 500f),
                shape = starShape(500f, 500f, 65f, 32f, 6)
            )
        )

        return ArtworkTemplate(
            id = "crystal_dragon_medallion",
            titleTr = "Kristal Ejderha Madalyonu",
            artistNoteTr = "Geometrik kristal yüzeyler ve kadim yıldız enerjisiyle parlayan tılsım.",
            category = ArtCategory.COSMIC,
            difficulty = DifficultyLevel.MEDIUM,
            defaultPaletteId = "cosmic_nebula",
            regions = regions
        )
    }
}
