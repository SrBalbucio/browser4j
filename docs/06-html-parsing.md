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
> Não é possível fazer Live-Tracking de instâncias baseadas no DOM Jsoup retornado. Você precisará chamar `getDOM()` frequentemente em momentos cruciais da navegação para obter um Snapshot atualizado. Abordagens de "tempo real" serão exploradas na biblioteca futuramente com base em Mutational Observers.
