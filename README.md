
# Emulador ZPL - Automacao WebIA

Aplicação desktop desenvolvida em **Java Swing** para emular a impressão de etiquetas no padrão **ZPL (Zebra Programming Language)**. Além disso, permite gerar pré-visualizações das etiquetas, enviar arquivos para impressão a laser ou outros dispositivos, bem como gerenciar templates e dados dinâmicos.

Este projeto faz parte das soluções oferecidas pela [Automacao WebIA](https://automacaowebia.com.br), atendendo a demandas de automação, logística, controle de produção e rastreabilidade.

---

## 🛠️ Tecnologias e Ferramentas Utilizadas

- ✅ Java 17 (compatível com Java 11+ e Java 21)
- ✅ Java Swing (interface gráfica)
- ✅ JasperReports (Geração de relatórios e PDFs)
- ✅ PostgreSQL (Banco de dados relacional)
- ✅ Apache Commons Email (Envio de e-mails com anexos e templates)
- ✅ Jakarta Mail e Jakarta Activation (Manipulação de e-mails no Java 11+)
- ✅ JCalendar (Componente de calendário no Swing)
- ✅ Maven (Gerenciamento de dependências e build)
- ✅ Docker (para banco ou outros serviços — opcional)
- ✅ Labelary API ou processamento interno de ZPL (compatível)

---

## 📦 Funcionalidades do Projeto

- 🎯 **Emulador de ZPL:**
  Permite gerar uma pré-visualização da etiqueta antes da impressão.

- 🖨️ **Impressão direta:**
  Integração com impressoras a laser, térmicas ou sistemas de marcação industrial.

- 🧠 **Templates Dinâmicos:**
  Sistema de templates com dados variáveis como SKU, lote, número de série, descrição, etc.

- 📤 **Envio de e-mails:**
  Permite enviar relatórios, comprovantes ou etiquetas em PDF diretamente via e-mail.

- 🗂️ **Relatórios JasperReports:**
  Geração de relatórios personalizados e exportação para PDF, XLS, etc.

- 🔗 **Integração com Banco de Dados:**
  Armazena logs, templates, parâmetros e histórico de impressão.

- 🔒 **Backup e Restauração:**
  Gerenciamento seguro dos dados da aplicação.

---

## 🚀 Como Executar

### ✅ Pré-requisitos:

- Java JDK 17 (ou superior)
- PostgreSQL instalado (local ou remoto)
- Maven instalado (ou use o Maven Wrapper)

### ✅ Clonar o projeto:

```bash
git clone https://github.com/SEU_USUARIO/emulador-zpl.git
cd emulador-zpl
```

### ✅ Configurar o banco de dados:

1. Crie o banco no PostgreSQL:

```sql
CREATE DATABASE emuladorzpl;
```

2. Configure as credenciais no arquivo:

```
src/main/resources/config.properties
```

Exemplo:

```
db.url=jdbc:postgresql://localhost:5432/emuladorzpl
db.user=seu_usuario
db.password=sua_senha
```

### ✅ Build do projeto:

```bash
mvn clean package
```

O JAR gerado estará em:

```
target/emulador-zpl-1.0.0-jar-with-dependencies.jar
```

### ✅ Executar:

```bash
java -jar target/emulador-zpl-1.0.0-jar-with-dependencies.jar
```

---

## 🔗 String de Conexão PostgreSQL

```java
String url = "jdbc:postgresql://localhost:5432/emuladorzpl";
String user = "seu_usuario";
String password = "sua_senha";

Connection conn = DriverManager.getConnection(url, user, password);
```

---

## 📂 Estrutura do Projeto

```
emulador-zpl/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/automacaowebia/
│   │   │        ├── App.java
│   │   │        ├── views/
│   │   │        ├── controller/
│   │   │        └── services/
│   │   └── resources/
│   │        ├── templates/
│   │        ├── images/
│   │        └── config.properties
├── target/
├── README.md
└── pom.xml
```

---

## 🗒️ Scripts SQL Exemplos

```sql
CREATE TABLE templates (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    conteudo_zpl TEXT NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW()
);

CREATE TABLE historico_impressoes (
    id SERIAL PRIMARY KEY,
    template_id INTEGER REFERENCES templates(id),
    parametros JSONB,
    data_impresso TIMESTAMP DEFAULT NOW()
);
```

---

## 💻 Telas da Aplicação (Exemplos)

- Interface para seleção de templates
- Tela de pré-visualização de etiquetas ZPL
- Gerenciador de históricos de impressão
- Formulário para envio de relatórios via e-mail
- Relatórios Jasper gerados em PDF

*(Adicione capturas de tela na pasta `/docs` ou `/images`)*

---

## 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se livre para abrir issues, enviar pull requests ou sugerir melhorias.

---

## 🧑‍💻 Desenvolvido por

**[Automacao WebIA](https://automacaowebia.com.br)**
Soluções em automação, inteligência de dados, logística e processos industriais.

---

## 📜 Licença

Este projeto é privado e protegido. Distribuição, modificação ou uso sem autorização da Automacao WebIA não é permitido.

---
