# SporeDoc — Visão para o planner

> Input para a skill `planner` (Modo 1, projeto novo). Colar na conversa com o planner.
> Projeto: `sporedoc`. Prefixo de IDs: `SD-`.

## Produto

SporeDoc é um scanner de documentos para Android, da suite "Knowledge Applications". O utilizador digitaliza papéis (faturas, contratos, recibos), guarda-os como PDF no telemóvel e partilha-os. É uma app utilitária: abre, digitaliza, partilha, fecha.

Para quem: uma pessoa que precisa de transformar papel em PDF rapidamente e enviá-lo, sem conta nem cloud.

## Design (fonte de verdade visual)

O design está aprovado e vive no repositório da app, não nas specs:

- `docs/design/sporedoc-handover.json` — tokens, componentes, ecrãs, acessibilidade.
- `docs/design/sporedoc-screens.pdf` — referência visual dos dois ecrãs.

As specs referenciam secções do JSON pelo caminho (ex. `components.DocumentRow`, `screens.ViewerScreen.footer`), não copiam valores. A fidelidade visual é verificada por mim no PR contra o PDF.

## Âmbito do MVP

A app tem dois ecrãs próprios: lista de documentos (`DocumentsScreen`) e visualizador (`ViewerScreen`). Captura, deteção de margens, crop e filtros são do Google ML Kit Document Scanner, não da app.

1. **Tema.** O tema Compose implementa os tokens do handover: cores, tipografia (Anton + JetBrains Mono), 0dp de cantos, strokes de 0.5dp/1dp, fundo em gradientes, vinheta e grão.
2. **Capturar.** O botão CAPTURE abre o ML Kit Document Scanner. O resultado é guardado como PDF no armazenamento interno da app, e a lista atualiza-se. A importação de imagens da galeria é feita pela opção nativa do próprio scanner.
3. **Listar.** A lista mostra todos os PDFs, do mais recente para o mais antigo, agrupados em TODAY (mesmo dia de calendário), RECENT DOCUMENTS (últimos 30 dias, excluindo hoje) e ARCHIVE (mais antigos). Cada linha mostra a miniatura da primeira página, o nome e `{páginas} PP / {tamanho} MB / {dd.MM.yyyy HH:mm}`. Com a lista vazia, aparece um estado vazio com uma linha de texto e o CAPTURE visível.
4. **Ver.** Tocar numa linha abre o visualizador com as páginas em paginação horizontal e o indicador `PAGE x / y`.
5. **Partilhar / exportar.** "[ SHARE ]" na linha, "Share" no menu ⋮ e "EXPORT DOCUMENT (PDF)" no visualizador abrem todos a share sheet nativa do Android com o PDF.
6. **Gerir.** O menu ⋮ de cada linha tem Rename, Share e Delete. O ícone de apagar no visualizador também apaga. Apagar pede sempre confirmação.
7. **Importar PDF.** O ícone de importar (canto superior direito) escolhe um PDF existente no telemóvel e copia-o para a biblioteca da app.

## Regras de negócio

- Nome por omissão: `Spore_yyyyMMdd_HHmm.pdf`, com a hora do momento da captura. Se esse nome já existir, acrescenta-se `_2`, `_3`…
- Um PDF importado mantém o nome original. Se colidir, segue a mesma regra do sufixo.
- Rename altera o nome do ficheiro. Não pode ficar vazio nem repetir um nome existente; a extensão `.pdf` é mantida automaticamente.
- Apagar remove o ficheiro do dispositivo. Não há lixo nem desfazer.
- Os diálogos de rename e de confirmação seguem os mesmos tokens (0dp de cantos, bordas douradas de 0.5dp, tipografia mono).
- O texto da UI é em inglês, tal como no handover.

## Acessibilidade (critérios verificáveis)

- Todos os alvos de toque têm pelo menos 44dp.
- Os content descriptions são os do handover (`accessibility.contentDescriptions`).
- Grão, vinheta, orbe, retículas e molduras das miniaturas não são anunciados pelo TalkBack.

## Fora de âmbito

OCR, contas, cloud ou sync, assinaturas, exportação em JPEG, seleção múltipla e long-press, pastas e etiquetas, pesquisa.

## Restrição técnica dada

Usar o Google ML Kit Document Scanner API para captura. O resto da arquitetura (persistência, estrutura) é decidido nas corridas, não aqui.
