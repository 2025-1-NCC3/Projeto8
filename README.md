```
# 💛 Uber SafeStart

## ✨ Segurança Inteligente e Respeito Mútuo para Motoristas e Passageiros

## 🛠️ Tecnologias Utilizadas
- **Plataforma:** Android Studio (futuro); Java puro (atual)
- **Linguagem:** Java
- **Interface:** XML (planejado)
- **Banco de Dados:** MySQL (local via Room, planejado)

## 🎯 Objetivo
Uber SafeStart eleva a segurança no transporte por aplicativo, prevenindo riscos antes e durante as viagens, promovendo respeito mútuo e protegendo contra assédio ou preconceito, seja do motorista ou do passageiro. Com alertas proativos, gamificação educativa e ferramentas inteligentes, criamos uma experiência confiável e acolhedora para todos, começando com uma base em Java que será expandida para Android.

## 👥 Integrantes do Projeto
- **Guilhermy Mariano**
- **Gustavo Bernardi**
- **Gustavo Oliveira Demetrio**
- **Saulo Pereira de Jesus**

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

**Sem Áudio:**
- A investigação considera histórico de comportamento, avaliações e suporte em tempo real.

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

## 🎨 Design do Projeto
O design e planejamento visual do Uber SafeStart foi criado no **Figma**. Acesse o layout completo **[aqui](#)**.

## 📚 Referências
- [Documentação do Java](https://docs.oracle.com/en/java/)
- [Documentação do Android Studio](https://source.android.com/docs?hl=pt)
- [Documentação do MySQL](https://dev.mysql.com/doc/)

## 📄 Licença
<p xmlns:cc="http://creativecommons.org/ns#" xmlns:dct="http://purl.org/dc/terms/"><span property="dct:title">Uber SafeStart</span> by <span property="cc:attributionName">Guilhermy Mariano, Gustavo Bernardi, Gustavo Demetrio, Saulo Pereira</span> is licensed under <a href="https://creativecommons.org/licenses/by-nc/4.0/?ref=chooser-v1" target="_blank" rel="license noopener noreferrer" style="display:inline-block;">CC BY-NC 4.0<img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1" alt=""></a></p>
```
