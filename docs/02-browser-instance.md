# Instâncias do Navegador no Browser4j

O gerenciamento principal das abas navegadas e as injecções Swing / UI ocorrem pela interface `balbucio.browser4j.browser.api.Browser` e a implementação `CefBrowserImpl`.

Um único contexto de `BrowserRuntime` pode instânciar diversos `Browser` independentes.

---

### Inicialização e Integração

Lembre-se de primeiro iniciar o [Runtime](01-runtime-config.md) de fundo e depois você estará elegível a instanciar contextos usando a fábrica `CefBrowserImpl.create(CefApp p)`.

```java
import balbucio.browser4j.core.runtime.BrowserRuntime;
import balbucio.browser4j.browser.api.CefBrowserImpl;
import balbucio.browser4j.browser.api.Browser;
import balbucio.browser4j.browser.api.BrowserOptions;
import balbucio.browser4j.browser.api.Session;

// 1. (Recomendado) Utilize Sessions para manter consistência de Rede e Fingerprint:
// Leia mais na seção [04 Proxy Pool e Fingerprint](04-proxy-pool.md)
// Session session = Session.create(...);

// 2. Opcionalmente configurar isolamentos de perfil do browser instanciado:
BrowserOptions opStatus = BrowserOptions.builder()
        // Substitui o User Agent para apenas esta "aba" - caso necessário em futuras versões
        .userAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")
        // ou defina a Session completa:
        // .session(session)
        .build();

// 3. Crie a Interface
Browser browser = CefBrowserImpl.create(BrowserRuntime.getCefApp(), opStatus);

// Se nenhuma opção for requerida, pode invocar CefBrowserImpl.create(BrowserRuntime.getCefApp());
```

---

### Obtendo Componente para JFrame / AWT
Após criada a instância, deve-se acoplar a sua interface visual caso não esteja ativado a opção `--osrEnabled=true`. Para acoplar ao Swing:

```java
import javax.swing.JFrame;
import java.awt.Component;

JFrame frame = new JFrame("Minha App Web");
Component meuComponenteNavegador = ((CefBrowserImpl) browser).getView().getUIComponent();

frame.getContentPane().add(meuComponenteNavegador, java.awt.BorderLayout.CENTER);
frame.setVisible(true);
```


### Comandos de Controle (Ações de Navegação e DOM)

A gerência e comunicação com a Engine se dá utilizando métodos declarados:

#### Comandos Básicos
```java
browser.loadURL("https://github.com");
browser.reload();

// Navegação entre histórico (após acionar páginas)
if (podeVoltar) {
    browser.goBack();
}
browser.goForward();
```

#### Comandos de Entrada (Interação Manual / Sem Mouse)
Se você estiver utilizando a modalidade robótica/scraper pode enviar entrada virtualmente pelas coordenadas do Frame renderizado via `InputController`:

```java
balbucio.browser4j.browser.input.InputController controller = browser.getInputController();

// Click em coordenadas absolutas
controller.click(150, 200); 

// Inserir texto cru ou enviar tecla tab via keyCode Java.awt.event.KeyEvent 
controller.type("Minha Query de busca...");
```

---

#### Console de Desenvolvedor Automático (Mensagens Web)
O Browser4j pode resgatar requisições feitas na linha de depuração do F12 (DevTools > `console.log(...)`) utilizando o callback global atrelado aquela instância:

```java
browser.onConsoleMessage((String logMensagem) -> {
    System.out.println("LOG JAVASCRIPT: " + logMensagem);
});
```

---

[Próximo: Lidando com Eventos, API de Rede, Cookies e Segurança →](03-events-network.md)
