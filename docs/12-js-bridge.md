# Modular JS Bridge

O Browser4j fornece um sistema modular para expor lógica Java diretamente ao JavaScript. Isso permite criar uma ponte (bridge) entre as duas linguagens de forma estruturada, segura e intuitiva.

---

## 🏗️ Conceitos Principais

1.  **`BridgeModule`**: Uma interface que marca uma classe Java como um módulo que pode ser exposto ao navegador.
2.  **`@BridgeMethod`**: Uma anotação que identifica quais métodos públicos de um módulo devem ser acessíveis pelo JavaScript.
3.  **Injeção Automática**: O Browser4j gera e injeta automaticamente um proxy JavaScript para cada módulo registrado, possibilitando chamadas assíncronas baseadas em **Promises**.

---

## 🚀 Como Criar um Módulo

Para criar um módulo, implemente a interface `BridgeModule` e marque os métodos desejados:

```java
import balbucio.browser4j.bridge.api.BridgeModule;
import balbucio.browser4j.bridge.annotations.BridgeMethod;

public class MySystemModule implements BridgeModule {

    @Override
    public String getName() {
        return "mySystem"; // Nome que será usado no JS (window.mySystem)
    }

    @BridgeMethod
    public String getVersion() {
        return "1.0.0";
    }

    @BridgeMethod
    public int sum(int a, int b) {
        return a + b;
    }
}
```

---

## 🔌 Registrando o Módulo

Após criar a instância do seu módulo, registre-o no `JSBridge` do seu navegador:

```java
Browser browser = ...;
browser.jsBridge().registerModule(new MySystemModule());
```

---

## 🌐 Usando no JavaScript

O Browser4j injetará automaticamente um objeto global com o nome definido no seu módulo. Todos os métodos marcados retornarão uma **Promise**.

```javascript
// Chamadas simples
window.mySystem.getVersion().then(version => {
    console.log("Versão do Java: " + version);
});

// Com argumentos
window.mySystem.sum(10, 20).then(result => {
    console.log("Resultado da soma no Java: " + result);
});

// Usando async/await
async function checkBridge() {
    try {
        const res = await window.mySystem.sum(5, 5);
        console.log(res);
    } catch (err) {
        console.error("Erro na bridge:", err);
    }
}
```

---

## ⚡ Suporte Assíncrono (CompletableFuture)

Se o seu método Java for demorado, você pode retornar um `CompletableFuture`. A bridge aguardará a conclusão antes de resolver a Promise no JavaScript.

```java
@BridgeMethod
public CompletableFuture<String> fetchExternalData() {
    return CompletableFuture.supplyAsync(() -> {
        // Simula operação pesada
        Thread.sleep(2000);
        return "Dados processados";
    });
}
```

---

## 🔒 Segurança e Tipagem

*   **Conversão de Dados**: Os argumentos passados pelo JS são convertidos automaticamente para os tipos Java correspondentes usando Jackson.
*   **Privacidade**: Apenas métodos marcados com `@BridgeMethod` são expostos. Métodos internos da classe permanecem seguros.
*   **Tratamento de Erros**: Se o método Java lançar uma exceção, a Promise no JavaScript será rejeitada com os detalhes do erro.

---

[← Voltar: Cache Avançado](11-cache-manager.md) | [Home →](../get-started.md)
