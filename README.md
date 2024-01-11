# Rest Api  with Spring Boot and Java

[![Docker Hub Repo](https://img.shields.io/docker/pulls/cristian19952/rest-api.svg)](https://hub.docker.com/repository/docker/cristian19952/rest-api)
---
- ### Prática do curso REST API's RESTFul do 0 à AWS c. Spring Boot 3 Java e Docker
- [Link do curso](https://www.udemy.com/course/restful-apis-do-0-a-nuvem-com-springboot-e-docker/)
---

## Como Executar

Para executar este projeto, você precisará do JDK 21 ou mais recente e do Maven instalados em sua máquina.

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/cristian-95/rest-api-spring.git
   ```

2. **Navegar para o diretório do projeto:**
   ```bash
   cd rest-api
   ```

3. **Compilar e empacotar o aplicativo:**
   ```bash
   mvn clean package
   ```

4. **Executar o aplicativo:**
   ```bash
   java -jar target/vuttr-0.0.1-SNAPSHOT.jar
   ```

5. **Acessar a aplicação:**

   Uma vez que a aplicação esteja em execução, você pode acessá-la em `http://localhost:8080` no seu navegador.

**OBS**: Certifique-se de criar um banco de dados e preencher o arquivo de configuração application.yaml corretamente
para poder se conectar ao banco de dados MySQL.
