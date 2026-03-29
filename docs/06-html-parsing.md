# Extração de HTML e Parsing com Jsoup

Para fins de scraping e bots, é extremamente útil extrair o conteúdo do DOM nativo sem depender estritamente de execuções JavaScript ponteadas.

O Browser4j fornece uma abstração direta para resgatar o código-fonte da aba atual já parseado utilizando a popular biblioteca **Jsoup**.

## 1. Obtendo o Documento (Snapshot)

A função `getDOM()` retorna uma `CompletableFuture<Document>`. A extração do código nativo passa pelo pipeline do CEF de forma Assíncrona para não travar a UI (Thread principal).

```java
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import balbucio.browser4j.ui.tab.Tab;

// ... após criar sua Tab ...
tab1.getBrowser().getDOM().thenAccept((Document doc) -> {
    System.out.println("Título extraído: " + doc.title());
    
    // Usufruindo do poder do Jsoup
    Elements links = doc.select("a[href]");
    links.forEach(link -> {
         System.out.println("Link encontrado: " + link.attr("href"));
    });
}).exceptionally(ex -> {
    ex.printStackTrace();
    return null;
});
```

## ⚠️ Limitações Importantes: Estado "Snapshot"

> [!WARNING]
> O método `getDOM()` captura o estado do HTML e DOM correspondente **ao exato milissegundo em que foi invocado**.
> 
> Ele age como uma **cópia estática (Snapshot)**. Se a página web continuar mudando após a captura (exemplo: novos elementos adicionados via WebSockets, animações React/Vue alterando as classes, ou requisições AJAX chegando tardiamente), **estas alterações não serão refletidas** no objeto `Document` retornado.
>
> Não é possível fazer Live-Tracking de instâncias baseadas no DOM Jsoup retornado. Você precisará chamar `getDOM()` frequentemente em momentos cruciais da navegação para obter um Snapshot atualizado.

## 2. Observação de mudanças no DOM em tempo real (MutationObserver)

Foi adicionada uma nova API para acompanhar mutações do DOM em tempo real, usando `MutationObserver` injetado no contexto da página:

- `Browser.addDomMutationListener(DomMutationListener listener)`
- `Browser.removeDomMutationListener(DomMutationListener listener)`

A cada mutação válida (`childList`, `attributes`, `characterData`), o Browser4j envia evento `dom_mutation` através da ponte JSBridge para Java.

### Exemplo de uso

```java
import balbucio.browser4j.browser.events.DomMutationEvent;
import balbucio.browser4j.browser.events.DomMutationListener;

browser.addDomMutationListener(new DomMutationListener() {
    @Override
    public void onDomMutation(DomMutationEvent event) {
        System.out.println("DOM mutation: " + event.getType());
        System.out.println("Target: " + event.getTargetTag() + " id=" + event.getTargetId());
        System.out.println("OuterHTML: " + event.getOuterHTML());
        System.out.println("Added elements: " + event.getAddedOuterHTML());
        System.out.println("Removed elements: " + event.getRemovedOuterHTML());
        System.out.println("Attribute: " + event.getAttributeName() + " old=" + event.getOldValue());
    }
});
```

### Quando usar

- Raspagem em páginas altamente dinâmicas (SPA, atualizações em tempo real, data grids)
- Validação de estrutura dinâmica (elementos adicionados/removidos/alterados)
- Triggers de automação reativos a mudanças de DOM

### Observações

- O evento é enviado em lote após cada observação de mutation e pode conter várias mudanças por emissão.
- Pode gerar muito tráfego para alterações intensas; use lógica de filtro no listener para evitar sobrecarga.
- Esse mecanismo complementa `getDOM()` e permite use cases de monitoramento contínuo enquanto a página está ativa.

