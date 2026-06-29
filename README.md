MLOps Pipeline Management API

This project is a JAX-RS RESTful API for managing machine learning workspaces, machine learning models and evaluation metrics. It was developed for the Client-Server Architectures coursework using Java, Maven, Jersey/JAX-RS and Apache Tomcat 9.
The API uses an in-memory data store with HashMap and ArrayList. No external database is used.

Base URL: http://localhost:8081/mlops-pipeline-api/api/v1

Build and Run Instructions:
Requirements
Java JDK
Apache NetBeans
Apache Tomcat 9
Maven
Postman

Build Steps:
Open the project in Apache NetBeans.
Right-click the project.
Select Clean and Build.
Wait until the output shows BUILD SUCCESS.

Deployment Steps:

This project was tested using manual WAR deployment on Apache Tomcat 9.

1. Open the project in NetBeans
2. Right click and select Clean and Build
3. Wait for BUILD SUCCESS
4. Grab the WAR from target/mlops-pipeline-api.war
5. Drop it into your Tomcat webapps folder
6. If there's an old mlops-pipeline-api folder in there already, delete it first
7. Start Tomcat, it'll extract and deploy automatically
8. Test it at http://localhost:8081/mlops-pipeline-api/api/v1


<<<<<<< HEAD
=======

>>>>>>> 41dab9f8b42e08965056a8751362644e03658879
curl Examples

1. Discovery

curl -X GET http://localhost:8081/mlops-pipeline-api/api/v1

2. Get all workspaces

curl -X GET http://localhost:8081/mlops-pipeline-api/api/v1/workspaces

3. Create a workspace

curl -X POST http://localhost:8081/mlops-pipeline-api/api/v1/workspaces \
-H "Content-Type: application/json" \
-d "{\"teamName\":\"Robotics AI Lab\",\"storageQuotaGb\":300}"

4. Create a model

curl -X POST http://localhost:8081/mlops-pipeline-api/api/v1/models \
-H "Content-Type: application/json" \
-d "{\"framework\":\"Keras\",\"status\":\"TRAINING\",\"latestAccuracy\":0.66,\"workspaceId\":\"WS-VISION-01\"}"

5. Filter models by status

curl -X GET "http://localhost:8081/mlops-pipeline-api/api/v1/models?status=DEPLOYED"

6. Add a metric to a model

curl -X POST http://localhost:8081/mlops-pipeline-api/api/v1/models/MOD-8832/metrics \
-H "Content-Type: application/json" \
-d "{\"accuracyScore\":0.95}"

7. Delete a workspace that still has models (expect 409)

curl -X DELETE http://localhost:8081/mlops-pipeline-api/api/v1/workspaces/WS-VISION-01

QUESTIONS ANSWERS:

Part 1: Service Architecture & Setup

Question: When returning a Java object from a method, it is automatically serialised into
JSON. Explain the role of a MessageBodyWriter or a JSON provider (like Jackson) in this conversion Process/

A: When a JAX-RS resource method returns a plain Java object, the framework needs a way to convert that object into a format suitable for an HTTP response. This conversion is handled by a MessageBodyWriter or a JSON provider. The JAX-RS runtime checks the return type and the requested media type, such as application/json, then selects a suitable provider to serialize the Java object.
Jackson provides this functionality through a pluggable JSON provider. It inspects the Java object’s fields and getter methods using reflection, then converts the object into a JSON response body string. In this project, this setup allows resources such as MLWorkspace, MachineLearningModel, and EvaluationMetric to be returned directly from resource methods while still producing standard JSON payloads for the client.

Question: REST architecture dictates that APIs should be strictly ’stateless’. Define what
statelessness means in this context and explain why it makes cloud APIs easier to scale
horizontally across multiple servers.

A: In REST architecture, statelessness means the server does not store client session information between requests. Each HTTP request must contain all the information needed for the server to process it. For example, if authentication or context is required, that information must be sent with every request rather than relying on stored server-side session data.
This property makes cloud APIs much easier to scale horizontally because any server instance can handle any incoming request. A load balancer can freely distribute traffic across a cluster, sending one request to Server A and the next request to Server B, without breaking the application state. This eliminates the need for complex sticky sessions and removes reliance on a shared server-side session store, resulting in a scalable and fault-tolerant system that is easier to deploy across multiple cloud servers.


Part 2: Workspace Management

Question: Discuss how implementing HTTP Cache-Control headers on the GET workspaces endpoint could improve performance for the client and reduce unnecessary processing load on the server.

A: Adding HTTP Cache-Control headers to the GET /api/v1/workspaces endpoint improves performance by telling clients and intermediate proxies exactly how long they may reuse a response before requesting it again. For example, a header like Cache-Control: max-age=60 allows the client to cache the workspace list locally for 60 seconds.
This configuration improves client performance because repeated requests within that window are served instantly from the local cache, eliminating network round-trip latency. It also reduces server workload because fewer requests actually hit the application layer. In this project, caching prevents unnecessary repeated access to the underlying workspace collection, lowering CPU utilisation. For more precise cache management, this can be paired with ETags, allowing the server to quickly return a 304 Not Modified status code without a response body if the data has not changed.

Question: If a client needs to verify whether a specific workspace exists but wants to save bandwidth by not downloading the entire JSON body, which HTTP method should they use instead of GET? Explain your reasoning.

A: The client should use the HTTP HEAD method instead of GET. The HEAD method is similar to GET, meaning the server performs the same type of resource lookup and returns the same status code logic, but it strips the response body entirely before transmission.
If the requested workspace exists, the server returns a 200 OK status. If it does not exist, it returns 404 Not Found. This saves significant network bandwidth because the client can verify whether a resource is present based purely on the status code, without downloading the full JSON representation of the workspace data.


Part 3: Model Operations & Linking

Question: When creating a new Model via a POST request, it is considered best practice for the server to generate the unique id (e.g., using UUID.randomUUID()) rather than allow-ing the client to pass an id in their JSON payload. Discuss the security and data integrity reasons behind this architectural choice.

A: Data integrity: Allowing clients to choose resource IDs introduces collision risks. Two different clients could independently submit the same ID, causing collisions or accidental overwriting of an existing model. Offloading ID creation to the server using UUID.randomUUID() ensures that each model receives a globally unique, consistently structured identifier without requiring external coordination.
Security: Client-supplied IDs expose additional attack risks. A malicious client could guess or pass specific resource IDs belonging to other projects to hijack or manipulate data. They could also use predictable or sequential IDs to infer the existence of other resources in the data store. Randomly generated server UUIDs are non-sequential and difficult to predict, which makes resource enumeration attacks much harder.

Question: If a user attempts to search for a framework containing spaces or special char-acters (e.g., ?framework=Scikit Learn & Tools), how must the client modify the URL, and why is this encoding necessary?

A: If a query parameter contains spaces or special characters, the client must apply URL percent-encoding before sending the request. For example, the query string ?framework=Scikit Learn & Tools must be modified to:
/api/v1/models?framework=Scikit%20Learn%20%26%20Tools
This encoding is necessary because characters such as ?, =, &, and literal spaces have explicit structural meanings within URI syntax. For instance, & acts as a delimiter separating different parameters. If these characters appear unencoded inside a parameter value, the HTTP parser may misinterpret them as structural delimiters. This could split the single value into multiple malformed parameters or cause parsing problems. Encoding escapes these characters into safe representations that the server can decode back to the original string.


Part 4: Deep Nesting with Sub-Resources

Question: You can place annotations like @Produces(MediaType.APPLICATION_JSON) at ei-ther the class level or the individual method level. What is the benefit of class-level place-ment, and how does method-level overriding work?

A: Placing @Produces(MediaType.APPLICATION_JSON) at the class level defines a default response media type for every resource method in that class. This follows the DRY, or “Don’t Repeat Yourself”, development principle, keeping the codebase cleaner, less error-prone, and easier to modify globally because the setting applies automatically to all methods in the class.
Method-level @Produces annotations are used to override this class default when a specific endpoint requires an alternative response format. For example, while the class defaults to JSON, a specific export endpoint could use @Produces(MediaType.TEXT_PLAIN) to output plain text. The JAX-RS runtime resolves these by specificity: the more local method-level annotation takes priority over the class-level annotation, allowing targeted flexibility without affecting other methods.


Part 5: Advanced Error Handling, Exception Mapping & Logging

Question: HTTP status codes are categorised into classes (e.g., 2xx, 4xx, 5xx). Explain fun-damentally why a validation failure caused by the user providing a non-existent workspaceId must return a 4xx code rather than a 5xx code.

A: HTTP status codes are categorised by the root cause of the response. The 4xx class indicates a client-side error, meaning the incoming request was malformed, unauthorised, or logically invalid, but the server processed it correctly and detected the fault. The 5xx class indicates a server-side failure, meaning the server encountered an unexpected runtime crash, data store failure, or infrastructure problem while trying to process a valid request.
When a client provides a non-existent workspaceId during model creation, the server has executed its logic correctly. It received the request, validated the supplied workspace ID against the in-memory data store, and properly rejected the invalid reference. Because the root issue is the invalid input data supplied by the client, returning a 5xx code would be incorrect. It would misinform client monitoring tools that the server is broken when the client simply needs to fix their payload. A client error code like 400 Bad Request or 422 Unprocessable Entity is the architecturally correct response.

Question: If an operation throws a specific custom exception
(e.g., LinkedWorkspaceNotFoundException) and you also have a global ExceptionMapper<Throwable>, how does the JAX-RS runtime determine which mapper to execute?

A:  The JAX-RS runtime determines which mapper to execute using a “most-specific-type-wins” resolution strategy based on the Java exception class hierarchy. When an exception is thrown, the runtime evaluates all registered ExceptionMapper implementations and binds the failure to the one whose generic type parameter most closely matches the thrown exception.
If the application throws a custom LinkedWorkspaceNotFoundException, JAX-RS skips the generic Throwable mapper and executes the explicit ExceptionMapper<LinkedWorkspaceNotFoundException>. The global ExceptionMapper<Throwable> serves as a catch-all safety net. It remains in the background to safely capture unexpected runtime issues, such as a NullPointerException, mapping them to a clean 500 Internal Server Error response so that raw system stack traces are not exposed to the client.

Question: In your filter, you interact with ContainerRequestContext and ContainerResponseContext. List two pieces of crucial HTTP metadata (e.g., headers, URIs) you can extract from these contexts that are highly valuable for debugging server issues.

A: Inside request and response filters, the ContainerRequestContext and ContainerResponseContext interfaces expose key HTTP metadata that is useful for diagnostic logging. Two highly valuable pieces of metadata are:
1.The request URI and HTTP method, retrieved using requestContext.getUriInfo().getRequestUri() and requestContext.getMethod(). These values identify the endpoint path targeted by the user and the HTTP verb used, which is the starting point for reproducing API issues.
2.The final response status code, retrieved using responseContext.getStatus(). This indicates the outcome of the request, such as 201 Created, 404 Not Found, 409 Conflict, or 500 Internal Server Error.
Logging this information alongside the request URI provides a clear and useful audit trail of API activity. It helps monitor service health, identify repeated errors, and understand client usage patterns.
