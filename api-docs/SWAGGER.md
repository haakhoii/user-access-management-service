docker run -d \
--name swagger-local-user \
-p 8888:8080 \
-e URLS='[
{"url":"http://localhost:8081/api/v1/auth/v3/api-docs","name":"Auth Service"},
{"url":"http://localhost:8082/api/v1/user/v3/api-docs","name":"User Service"}
]' \
swaggerapi/swagger-ui:v5.29.3
