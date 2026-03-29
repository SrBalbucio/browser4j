# Getting Started com Browser4j

O **Browser4j** é uma biblioteca Java para embutir um navegador avançado baseado no Chromium (via JCEF) na sua aplicação, oferecendo controle robusto de networking, eventos, segurança e renderização OSR/UI.

Este guia rápido vai te ensinar a configurar o projeto e inicializar o navegador em um aplicativo Swing.

## 1. Adicionando ao seu Projeto

O Browser4j requer Java 21+ e depende do JCEF (Java Chromium Embedded Framework) via `me.friwi:jcefmaven`.
Adicione no seu `pom.xml`:

```xml
<dependency>
    <groupId>balbucio.browser4j</groupId>
    <artifactId>browser4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> [!NOTE] 
> O Browser4j depende de arquiteturas 64-bits (Windows, Linux ou macOS).

## 2. Exemplo Completo (Swing)

O uso típico de `browser4j` com interface gráfica (Swing) envolve dois passos: 
1. **Inicializar o Runtime** globalmente.
2. **Criar a instância** do Browser e adicioná-la a um JFrame.

Aqui está o código completo:

```java
import balbucio.browser4j.core.config.BrowserRuntimeConfiguration;
import balbucio.browser4j.core.runtime.BrowserRuntime;
import balbucio.browser4j.browser.api.CefBrowserImpl;
import balbucio.browser4j.browser.api.Browser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Browser4jExample {

    public static void main(String[] args) {
        // 1. Configurar e Inicializar o Runtime (processo global do Chromium)
        BrowserRuntimeConfiguration config = BrowserRuntimeConfiguration.builder()
                .enableGPU(true)
                .cookiesPersistent(true)
                // Personalize as pastas de userData e Cache caso queira guardar o estado entre sessões:
                // .userDataPath("./userdata")
                // .cachePath("./cache")
                .build();

        BrowserRuntime.init(config);

        // 2. Aguardar a UI thread do Swing para criar a Janela (JFrame)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Browser4j - Getting Started");
            frame.setSize(1024, 768);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            // 3. Criar a Instância do Browser
            CefBrowserImpl browser = (CefBrowserImpl) CefBrowserImpl.create(BrowserRuntime.getCefApp());

            // 4. Injetar a Visualização UI (Componente AWT/Swing) no JFrame
            Component browserComponent = browser.getView().getUIComponent();
            frame.getContentPane().add(browserComponent, BorderLayout.CENTER);
            
            // Ouvir mensagens do console javascript
            browser.onConsoleMessage(msg -> System.out.println("Console JS: " + msg));

            // 5. Encerrar o BrowserRuntime corretamente ao fechar a janela
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    browser.close(); // Fecha a instância específica
                    BrowserRuntime.shutdown(); // Desliga o processo do Chromium
                    frame.dispose();
                    System.exit(0);
                }
            });

            frame.setVisible(true);

            // 6. Navegar para uma URL
            browser.loadURL("https://www.google.com");
        });
    }
}
```

## Próximos Passos
Consulte a documentação em `/docs` para mergulhar nas capacidades mais profundas da ferramenta:
* [Configurações do Runtime e Options](docs/01-runtime-config.md)
* [API do Browser e Navegação](docs/02-browser-instance.md)
* [Eventos, Rede, Cookies e Segurança](docs/03-events-network.md)
* [Gerenciamento de Cache Avançado](docs/11-cache-manager.md)
