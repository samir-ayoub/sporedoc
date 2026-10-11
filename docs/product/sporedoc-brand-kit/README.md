# SporeDoc — Ícone e Splash Screen

Kit de marca para Android: logótipo (pirâmide de latão dourado partida pelo feixe verde laser), ícone da app e splash screen.
Ver `preview/splash-and-launcher-preview.png` para o resultado final.

## 1. Os tamanhos importam?

O SVG é vetorial, mas o Android não usa o SVG tal como está, e cada sítio tem a sua grelha:

| Onde | Tela | Zona segura | Ficheiro |
|---|---|---|---|
| Ícone da app (ícone adaptável, API 26+) | 108 × 108 dp | círculo de 66 dp ao centro (o launcher corta o resto) | `ic_launcher_foreground.xml` + `ic_launcher_background.webp` |
| Ícone temático (Android 13+) | 108 × 108 dp | igual | `ic_launcher_monochrome.xml` |
| Splash do sistema (Android 12+ / core-splashscreen) | 288 × 288 dp | círculo de 192 dp (o sistema corta o resto) | `splash_icon.xml` |
| Play Store | 512 × 512 px PNG | quadrado inteiro | `preview/playstore-icon-512.png` |

Toda a arte está desenhada numa grelha de 108 com a pirâmide dentro do círculo de 66 dp. O mesmo desenho serve para o splash, escalado para 288 dp: a pirâmide fica dentro dos 192 dp.
Não precisas de PNG por densidade para o desenho. A única exceção é o fundo com grão (ver ponto 4).

## 2. Conteúdo

```
svg/
  sporedoc-logo-master.svg      arte completa (grão, latão escovado, brilho com blur). Para Play Store, marketing e previews
  ic_launcher_foreground.svg    versão segura para o Vector Asset Studio (sem filtros)
  ic_launcher_background.svg    gradiente do fundo, sem grão
  ic_launcher_monochrome.svg    silhueta para o ícone temático
  splash_icon.svg               marca para o splash (grelha de 108, exportada a 1152 px)
  mark_pyramid_only.svg         pirâmide + halo sem feixe (usada na splash da app)
android/res/
  drawable/ic_launcher_foreground.xml       VectorDrawable pronto, com gradientes aapt
  drawable/ic_launcher_background_vector.xml alternativa vetorial ao fundo, sem grão
  drawable/ic_launcher_monochrome.xml
  drawable/splash_icon.xml                  288 dp, para windowSplashScreenAnimatedIcon
  drawable/splash_mark.xml                  288 dp, sem feixe, para a BrandSplash em Compose
  drawable-nodpi/grain_tile.png             textura de ruído 256 px, repetida em mosaico
  mipmap-*/ic_launcher_background.webp      fundo nebulosa com grão a 8 %, nas 5 densidades
  mipmap-anydpi-v26/ic_launcher(_round).xml ícone adaptável (fundo + frente + monocromático)
  values/themes_splash.xml                  Theme.SporeDoc.Starting
android/compose/BrandSplash.kt              splash da app (textura, nebulosa, feixe, wordmark)
preview/
```

Usa os XML de `android/res` diretamente. Importar os SVG pelo Vector Asset Studio também funciona, mas os XML já vêm afinados.

## 3. A splash screen

O Android 12+ força uma splash do sistema que só aceita uma cor de fundo sólida e um ícone cortado em círculo. Não aceita gradiente, textura nem texto.
Para manter o clima "Absolute Elsewhere", a splash tem dois tempos ligados sem corte:

**Tempo 1: splash do sistema (0 ms).** Fundo abismo `#050508` com a marca ao centro. Dentro do círculo, o feixe aparece curto.

**Tempo 2: BrandSplash (Compose).** Começa num frame idêntico ao do tempo 1 e cresce a partir dele:

| ms | O que acontece |
|---|---|
| 0–400 | a nebulosa roxa (`#12091F` → `#030305`), a vinheta e o grão a 8 % aparecem em fade |
| 150–650 | o feixe verde cresce desde o limite do círculo até ao topo do ecrã |
| 450–750 | o halo do vértice pulsa uma vez |
| 500–900 | "SPOREDOC" (Anton, 40 sp, espaçamento 13 sp, marfim) sobe 8 dp em fade; "DOCUMENT SCANNER" (mono 8 sp, dourado fosco) aparece 150 ms depois |
| ≥ 900 | fica no frame final até a app estar pronta e passa aos Documents com crossfade de 250 ms |

Por baixo da pirâmide o feixe desvanece em 70 dp, para não cortar o wordmark.
"ELSEWHERE APPLICATIONS" fica na base, a 44 dp, em marfim a 42 %.
Com as animações desligadas no sistema, mostra logo o frame final.

### Integração

1. Copia `android/res/*` para `app/src/main/res/`.
2. Acrescenta a dependência `implementation("androidx.core:core-splashscreen:1.0.1")`.
3. No `AndroidManifest.xml`:
   - na `<application>`: `android:icon="@mipmap/ic_launcher"` e `android:roundIcon="@mipmap/ic_launcher_round"`;
   - na `MainActivity`: `android:theme="@style/Theme.SporeDoc.Starting"`.
4. Na `MainActivity.onCreate`, chama `installSplashScreen()` antes de `super.onCreate()`.
5. No NavHost, a rota inicial é a `BrandSplash(isReady = { viewModel.documentsLoaded }, onFinished = { navController.navigate("documents") { popUpTo("splash") { inclusive = true } } }, display = Anton, mono = JetBrainsMono)`.

## 4. Limitações a conhecer

- **Grão no ícone da app.** O VectorDrawable não suporta filtros de ruído nem blur. Por isso o grão vive no fundo em bitmap (`ic_launcher_background.webp`, 88 KB nas 5 densidades). A 48 dp no ecrã principal o grão mal se vê, como em qualquer textura fina. Onde se vê de facto é na BrandSplash. Se preferires um APK mais leve, troca o fundo pelo `ic_launcher_background_vector.xml` no `ic_launcher.xml`.
- **Cantos a 90°.** No ícone da app quem decide é o launcher: o Pixel corta em círculo e a Samsung em squircle. A Play Store também arredonda o PNG de 512 px. Os cantos retos só ficam garantidos dentro da app.
- **Brilho do feixe.** Sem blur no VectorDrawable, o brilho é feito com gradientes (halo radial no vértice e faixa transparente → verde → transparente à volta do feixe). O master SVG usa blur verdadeiro.
- **Fontes.** A BrandSplash usa Anton e JetBrains Mono, as mesmas dos ecrãs. Junta-as em `res/font/` ou usa as Google Fonts transferíveis.
