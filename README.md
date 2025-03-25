
# FECAP - Fundação de Comércio Álvares Penteado

<p align="center">
<a href="https://www.fecap.br/"><img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhZPrRa89Kma0ZZogxm0pi-tCn_TLKeHGVxywp-LXAFGR3B1DPouAJYHgKZGV0XTEf4AE&usqp=CAU" alt="FECAP - Fundação de Comércio Álvares Penteado" border="0"></a>
</p>

# Uber SafeStart

## Integrantes: [Guilhermy Mariano](https://www.linkedin.com/in/guilhermy-lisboa-garcia-385656223?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app), [Gustavo Bernardi](https://linkedin.com/in/gustavo-bernardi-r), [Gustavo Oliveira Demetrio](https://www.linkedin.com/in/gustavo-demetrio-145151270?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app), [Saulo Pereira de Jesus](https://www.linkedin.com/in/saulo-pereira-jesus?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app)

## 👨‍🏫 Professores Orientadores: [Katia Milani Lara Bossi](https://www.linkedin.com/in/katia-bossi/), [Marco Aurelio](https://github.com/fecaphub/Portfolio/blob/main), [Victor Rosetti](https://www.linkedin.com/in/victorbarq/) e [Vinicius Heltai](https://www.linkedin.com/in/vheltai/)

## Descrição
Uber SafeStart eleva a segurança no transporte por aplicativo, prevenindo riscos antes e durante as viagens, promovendo respeito mútuo e protegendo contra assédio ou preconceito, seja do motorista ou do passageiro. Com alertas proativos, gamificação educativa e ferramentas inteligentes, criamos uma experiência confiável e acolhedora para todos, começando com uma base em Java que será expandida para Android.

## 📊 Como Funciona?
### 1️⃣ Pré-Viagem: Preparação e Confiança
**Passageiro:**
- Recebe dicas de segurança, como "Mantenha a porta travada", "Fique atento ao trajeto" e "O botão de emergência está disponível".
- Confirma ações seguras, como verificar nome e placa do motorista, ganhando pontos no Safe Score.
- Vê um miniperfil do motorista (ex.: "Prefiro silêncio") e escolhe o tom da viagem: "Quero conversar", "Prefiro silêncio" ou "Apenas básico".
- Decide se aceita ou recusa a gravação de áudio, que só ocorre com consentimento mútuo.

**Motorista:**
- Visualiza a nota do passageiro, Street View do local de embarque e uma indicação da segurança da área com base em incidentes anteriores.
- Confirma a checagem do veículo, ganhando pontos no Badge de Motorista Consciente.
- Verifica o Badge de Confiança do passageiro para avaliar riscos.
- Também decide sobre a gravação de áudio.

**Ambos:**
- Confirmam um Código de Respeito contra assédio e preconceito, reforçando boas práticas.

### 2️⃣ Durante a Viagem: Segurança e Proteção
**Passageiro:**
- Recebe lembretes como "Já compartilhou sua rota?", ganhando mais pontos no Safe Score.
- Pode acionar um botão de emergência para reportar assédio do motorista, como comportamentos invasivos.

**Motorista:**
- Usa um alerta discreto para sinalizar assédio do passageiro, como insultos, com opção de encerrar a viagem em segurança.
- Responde a emergências do passageiro com instruções claras, recebendo reconhecimento.

**Gravação de Áudio:**
- Só ocorre com consentimento de ambos, sendo armazenada de forma segura por 30 dias. Sem gravação, o sistema registra dados de GPS (ex.: desvios) como evidência alternativa.

### 3️⃣ Pós-Viagem: Feedback e Resolução
**Passageiro:**
- Responde perguntas como "Você se sentiu respeitado(a)?", ganhando pontos por feedback detalhado.
- Denuncia assédio; a Uber analisa o áudio (se gravado) ou usa padrões e GPS.

**Motorista:**
- Reporta assédio do passageiro, como comentários inadequados, para o suporte.
- Avança no Badge de Motorista Consciente com boas práticas.

### 4️⃣ Banco de Dados
Armazenamento local com MySQL (via Room) guarda alertas, progresso de gamificação e denúncias, com integração futura a um backend para processar dados.

## 🚀 Diferenciais
- **Segurança Proativa:** Alertas antecipados, como Street View e dicas, previnem riscos.
- **Proteção Bidirecional:** Combate assédio de motorista ou passageiro com ferramentas específicas.
- **Gravação Opcional:** Áudio só com consentimento mútuo, acessado apenas em denúncias.
- **Alternativas Inteligentes:** Resolução sem áudio usando GPS e análise de padrões.
- **Experiência Integrada:** Segurança e respeito sem prejudicar a usabilidade.
- **Base Escalonável:** Projeto iniciado em Java, pronto para crescer em Android.

## 💥 Impacto Esperado
- 📈 Redução de incidentes e assédio bidirecional.
- 👨‍✈️ Aumento na retenção de motoristas por segurança e suporte.
- 👍 Melhora na confiança dos usuários na plataforma.
- 🌟 Cultura de respeito e responsabilidade mútua.
## 🛠 Estrutura de pastas

``` bash
  📁 Raiz
  │
  ├── 📁 docs
  │   └── 📁 Entrega1
  │         ├── 📄 DiagramaDeClasses.png
  │         ├── 📄 Entrega1_AnaliseDescritivaDeDados.xlsx
  │         └── 📄 UberSafeStart - Protótipo de Navegação [Figma].pdf
  │
  ├── 📁 images
  │
  ├── 📁 src
  │   └── 📁 Entrega1
  │         ├── 📁 backend
  │         ├── 📁 backendNODE
  │         └── 📁 frontend
  │
  ├── 📄 .gitignore
  └── 📄 README.md
```

A pasta raiz contém o arquivo:

<b>README.MD</b>: Guia e explicação geral sobre o projeto.




## 🎨 Design do Projeto
O design e planejamento visual do Uber SafeStart foi criado no **Figma**. Acesse o layout completo **[aqui](https://www.figma.com/design/fva6dhGFfB9Q0mpmNHT8YH/Uber-SafeStart?node-id=53-14&p=f&t=M4162DS9hsA04657-0)**.
Protótipo interativo **[aqui](https://www.figma.com/proto/fva6dhGFfB9Q0mpmNHT8YH/Uber-SafeStart?node-id=56-44&p=f&t=u702Ditftn5roEbT-0&scaling=min-zoom&content-scaling=fixed&page-id=53%3A14&starting-point-node-id=56%3A44)**.
## 🛠 Instalação

**Android:**

Em breve, será disponibilizado o APK via Android Studio.

```sh
(em construção)
```

## 💻 Configuração para Desenvolvimento

Para abrir este projeto você necessita das seguintes ferramentas:

- [Java JDK](https://www.oracle.com/java/technologies/downloads/)
- [Android Studio](https://developer.android.com/studio) (em breve)
- [MySQL](https://dev.mysql.com/downloads/mysql/)

## 📋 Licença
Este projeto é licenciado sob a licença Creative Commons [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/?ref=chooser-v1).

## 🎓 Referências

1. [Documentação Java](https://docs.oracle.com/en/java/)
2. [Android Studio](https://developer.android.com/docs)
3. [MySQL](https://dev.mysql.com/doc/)

