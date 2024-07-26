Metagraph - Data Application - Social
=============================

This example demonstrates a basic social media use case using the Data API. In the example, a client can send four types of signed data updates to a metagraph: one to create a post, another to edit a post, another to delete a post, and another to subscribe to a user. These updates are validated before being merged into the snapshot state.

Here's how the social media system works:

-   Each user can create, edit, and delete their own posts.
-   Each user can subscribe to other users to follow their posts.
-   Posts and subscriptions are validated to ensure their integrity and consistency.
-   Users cannot create duplicate posts with the same content.
-   This example persists the calculatedState in an external database, in this example PostgresSQL.
-   To use external storage, we've created a new service in the `Main.scala` files of both layers: l0 and data-l1. This new service implements the trait `ExternalStorageService`. Our `CalculatedStateService` receives an implementation of `ExternalStorageService` and calls the function to set the calculated state externally when we call the set function of `CalculatedStateService`. This function also was updated to work atomically.  
-   For more information, please refer to the section `Persisting User Calculated State to PostgreSQL`.
Template
--------

Primary code for the example can be found in the following files:

`modules/l0/src/main/scala/com/my/currency/l0/*`

`modules/l1/src/main/scala/com/my/currency/l1/*`

`modules/data_l1/src/main/scala/com/my/currency/data_l1/*`

`modules/shared_data/src/main/scala/com/my/currency/shared_data/*`

### Application Lifecycle

The methods of the DataApplication are invoked in the following sequence:

-   `validateUpdate`
-   `validateData`
-   `combine`
-   `dataEncoder`
-   `dataDecoder`
-   `calculatedStateEncoder`
-   `signedDataEntityDecoder`
-   `serializeBlock`
-   `deserializeBlock`
-   `serializeState`
-   `deserializeState`
-   `serializeUpdate`
-   `deserializeUpdate`
-   `setCalculatedState`
-   `getCalculatedState`
-   `hashCalculatedState`
-   `routes`

For a more detailed understanding, please refer to the [complete documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API.

### Routes

Customizes routes for our application.

In this example, the following endpoints are implemented:

-   GET `<metagraph l0 url>/data-application/users/:user_id/posts`: Returns all user posts.
-   GET `<metagraph l0 url>/data-application/users/:user_id/subscriptions`: Returns the subscriptions of a user.
-   GET `<metagraph l0 url>/data-application/users/:user_id/feed`: Returns the user's feed of posts from their subscriptions.

Scripts
-------

This example includes a script to generate, sign, and send data updates to the metagraph in `scripts/send_data_transaction.js`. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl` to match the configuration of your metagraph. You also must provide a private key representing the user that will create, edit, delete a post, or subscribe to another user (client) that is sending the transaction. This key will be used to sign the transaction and to log in your wallet to the network.

### Usage

-   With node installed, move to the directory and then type: `npm i`.
-   Replace the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables with your values.
-   Run the script with `node send_data_transaction.js`.
-   Query the state GET endpoint at `<your metagraph L0 base url>/data-application/posts` to see the updated state after each update.

Persisting User Calculated State to PostgreSQL
----------------------------------------------

The metagraph will persist the user calculated state into an external PostgreSQL database. This PostgreSQL database requires a table called `calculated_states` with two columns: `ordinal` (number) and `state` (jsonb). The database credentials should be provided in `application.json`.

To set up PostgreSQL using Euclid, add the following tasks to the end of `nodes.ansible.yml` after the task `Starting Docker container`:

```yaml
- name: Run PostgreSQL container
  docker_container:
    name: postgres-container
    image: postgres
    state: started
  restart_policy: always
  env:
  POSTGRES_USER: social
  POSTGRES_PASSWORD: social
  POSTGRES_DB: social
  networks:
  - name: custom-network
  published_ports:
  - "5432:5432"
  volumes:
  - /var/lib/postgresql/data

- name: Wait for PostgreSQL to start
  wait_for:
  host: 127.0.0.1
  port: 5432
  delay: 10
  timeout: 300

- name: Create init.sql file with table creation script
  copy:
  dest: /tmp/init.sql
  content: |
    CREATE TABLE calculated_states (
    ordinal BIGINT PRIMARY KEY,
    state JSONB
    );
- name: Copy init.sql to container
  command: docker cp /tmp/init.sql postgres-container:/docker-entrypoint-initdb.d/init.sql

- name: Execute init.sql in container
  command: docker exec -u postgres postgres-container psql social social -f /docker-entrypoint-initdb.d/init.sql

- name: Clean up init.sql file
  file:
  path: /tmp/init.sql
  state: absent
```
This setup ensures that the user calculated state is persisted in the PostgreSQL database, providing a reliable and scalable way to store and retrieve state data.