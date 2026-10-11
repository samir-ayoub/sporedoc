# SporeDoc

Document scanner for Android, part of the **Knowledge Applications** suite. Scan paper, save it as a PDF on the phone and share it. No account, no cloud.

## What it does (MVP)

- **Capture** with the Google ML Kit Document Scanner, which handles capture, cropping and filters. It can also import images from the gallery.
- **List** PDFs grouped into sections: TODAY, RECENT DOCUMENTS and ARCHIVE.
- **View** the pages of a document.
- **Share** through the native Android share sheet.
- **Rename and delete** documents.
- **Import** an existing PDF from the phone.

Out of scope: OCR, accounts, cloud, signatures and JPEG export.

## Requirements

- Android Studio with JDK 17.
- A device or emulator with Google Play Services. The ML Kit Document Scanner does not work without them.

## Commands

```bash
./gradlew assembleDebug        # build the app
./gradlew testDebugUnitTest    # run unit tests
./gradlew koverHtmlReport      # coverage report (once Kover is configured)
```

## Documentation

| File                                                                       | Contents |
|----------------------------------------------------------------------------|---|
| [`AGENTS.md`](AGENTS.md)                                                   | Architecture, folder structure and coding rules. Read before changing code. |
| [`docs/product/vision.md`](docs/product/vision.md)                         | Product vision, MVP scope and business rules. |
| [`docs/design/sporedoc-handover.json`](docs/design/sporedoc-handover.json) | Approved design: tokens, components and screens. Source of truth for visuals. |
| [`docs/design/sporedoc-screens.pdf`](docs/design/sporedoc-screens.pdf)     | Visual reference for both screens. |

## How it is developed

Deliveries are planned in specsbrain (`data/sporedoc/`) and implemented by Harness 2.0, one run per delivery, each with its own PR. The status of each delivery lives in specsbrain, not here.