# AGENTS.md — SporeDoc

Regras de código para qualquer agente (humano ou IA) que mexa neste repositório. As specs dizem **o quê**; este ficheiro diz **onde e como**. Em caso de conflito entre uma spec e este ficheiro, não escolhas: levanta a pergunta.

## Projeto

Scanner de documentos Android (suite Knowledge Applications). Visão em `docs/product/vision.md`. Design aprovado em `docs/design/sporedoc-handover.json`, com a referência visual em `docs/design/sporedoc-screens.pdf`.

## Stack

- Kotlin, Jetpack Compose, Material 3 (fortemente customizado).
- Coroutines e Flow para assincronia.
- Google ML Kit Document Scanner para captura.
- Navigation-Compose para navegação.
- Injeção de dependências manual (`AppContainer`), sem Hilt/Koin.
- Um só módulo Gradle: `:app`.
- Persistência: ficheiros no armazenamento interno da app. Se um dia houver base de dados, é Room.

Não acrescentes bibliotecas que a spec não peça. Se achares que uma é necessária, levanta a pergunta.

## Arquitetura

Três camadas, com dependências num só sentido:

```
ui  ──►  domain  ◄──  data
```

- **domain**: Kotlin puro. Não importa `android.*`, `androidx.*` nem bibliotecas de terceiros. Define os modelos, os casos de uso e as interfaces dos repositórios.
- **data**: implementa as interfaces do domain. É a única camada que toca em ficheiros, `ContentResolver`, `PdfRenderer` e, se existir, Room.
- **ui**: Compose e ViewModels. Depende só do domain e nunca importa nada de `data/`.

A ligação entre as camadas faz-se em `di/AppContainer.kt`, e só aí.

## Padrão de UI: MVVM com fluxo unidirecional

- Cada ecrã tem um `UiState` (data class imutável), exposto pelo ViewModel como `StateFlow<UiState>`.
- As ações do utilizador são funções públicas do ViewModel (`onRename(id, name)`), não setters de estado.
- Os efeitos de uma só vez (navegar, abrir a share sheet, mostrar um erro) saem por um `Flow<Effect>` do ViewModel, baseado num `Channel`.
- Cada ecrã divide-se em dois composables:
    - `XScreen(viewModel)` recolhe o estado e os efeitos;
    - `XContent(state, onAction…)` é stateless e é o que tem `@Preview`.
- Composables não têm lógica de negócio: formatar a data da linha, agrupar por secção ou validar um nome é trabalho do domain.

MVI completo (intents e reducers) não se usa: para dois ecrãs, o custo não compensa.

## Estrutura de pastas

Pacote base: `com.knowledge.sporedoc`.

```
app/src/main/java/com/knowledge/sporedoc/
├── SporeDocApp.kt              # Application, cria o AppContainer
├── MainActivity.kt             # host da navegação, mais nada
├── di/
│   └── AppContainer.kt
├── domain/
│   ├── model/                  # Document, DocumentSection…
│   ├── repository/             # interfaces (DocumentRepository…)
│   ├── usecase/                # um caso de uso por ficheiro
│   └── util/                   # funções puras partilhadas, nomeadas pelo assunto
├── data/
│   ├── repository/             # implementações das interfaces do domain
│   ├── file/                   # acesso a ficheiros, PdfRenderer, FileProvider
│   └── database/               # só se existir Room: entities, DAOs, database
└── ui/
    ├── theme/                  # tokens, cores, tipografia, efeitos
    ├── components/             # componentes partilhados por mais de um ecrã
    ├── navigation/             # rotas e NavHost
    ├── documents/              # DocumentsScreen, DocumentsViewModel, DocumentsUiState…
    └── viewer/                 # ViewerScreen, ViewerViewModel, ViewerUiState…
```

### Uma pasta nasce com o primeiro ficheiro que precisa dela

A árvore acima é o mapa, não um esqueleto a criar já. Não cries pastas vazias, interfaces sem segunda utilização prevista na spec, nem classes "para o futuro".

| Bloco | Quando nasce |
|---|---|
| `domain/usecase/X` | Quando há lógica a mais do que passar dados (agrupar, validar, resolver nomes repetidos). Uma chamada direta a um repositório não precisa de caso de uso; o ViewModel chama a interface do repositório. |
| `domain/util/` | Quando a mesma função pura é usada em dois sítios. Nunca um `Utils.kt` genérico: o ficheiro tem o nome do assunto (`FileNames.kt`). |
| `ui/components/X` | Quando o componente é usado por um segundo ecrã. Até lá, vive na pasta do ecrã. |
| `data/database/` | Só quando uma spec pedir algo que os ficheiros não resolvem (pesquisa, etiquetas). Nesse caso é Room. |

## Regras de colocação

| O quê | Onde |
|---|---|
| Lançar o ML Kit Scanner, abrir a share sheet, escolher ficheiro (`ActivityResultLauncher`, `Intent`) | `ui/`, no `XScreen`. O ViewModel recebe só o resultado (ex. o `Uri`). |
| Ler, gravar, renomear e apagar PDFs; contar páginas; gerar miniaturas | `data/file/`, atrás de uma interface do domain |
| Regras de nome (`Spore_yyyyMMdd_HHmm.pdf`, sufixo `_2`) e agrupamento TODAY / RECENT / ARCHIVE | `domain/usecase/` |
| Hora atual | Injetada (`Clock` ou `() -> Instant`), nunca `System.currentTimeMillis()` direto no domain, para ser testável |
| Dispatchers | Injetados, nunca `Dispatchers.IO` fixo dentro de classes testadas |

## Design system

- Todos os valores visuais vêm do handover JSON e vivem em `ui/theme/`. Os ecrãs não têm cores, `dp` ou `sp` literais: usam os tokens.
- Os nomes dos tokens em código seguem os do JSON (`Ivory`, `GoldMatte`, `Magma`…).
- Cantos sempre a 0dp (`RectangleShape`).
- O grão é uma textura pré-renderizada, nunca gerada por frame.
- Alvos de toque com pelo menos 44dp. Elementos decorativos (grão, vinheta, orbe, retículas, molduras) sem semântica para o TalkBack.
- O texto da UI vive em `strings.xml`, em inglês.

## Testes

- Os testes vivem em `src/test/` (unitários na JVM). O gate corre `testDebugUnitTest`, por isso testes em `src/androidTest/` não contam para validar uma entrega.
- Testa-se comportamento: casos de uso, ViewModels (transições de `UiState`, efeitos emitidos) e repositórios contra um diretório temporário.
- Usa fakes escritos à mão para as interfaces do domain. Bibliotecas de mocks só quando um fake for impraticável.
- Coroutines testadas com `kotlinx-coroutines-test` e dispatcher injetado.
- **Cobertura: alvo de 80%** em `domain/`, `data/` e nos ViewModels, medida com Kover. Ficam de fora os composables, `ui/theme/`, `di/`, `MainActivity` e `SporeDocApp`. O alvo serve para apanhar caminhos sem teste (erros, nomes repetidos, lista vazia), não para ser perseguido com testes sem asserções úteis.

## Código

- Código, identificadores e comentários em inglês.
- Uma classe pública por ficheiro, com o nome do ficheiro.
- Ficheiros pequenos e com uma só responsabilidade. Se um ViewModel ou repositório começa a fazer duas coisas, divide-o.
- Erros esperados (ficheiro em falta, nome repetido, scanner cancelado) devolvem-se como resultado tipado, não como exceção a atravessar camadas.
- Sem `GlobalScope`, sem `!!`, sem `lateinit` em ViewModels.

## Nunca

- Importar `android.*` ou `androidx.*` em `domain/`.
- Importar `data/` a partir de `ui/`.
- Pôr lógica de negócio em composables.
- Criar estrutura para uma necessidade que nenhuma spec pede.
- Acrescentar uma biblioteca sem a spec a pedir.
- Resolver em silêncio uma contradição entre a spec, este ficheiro e o design.