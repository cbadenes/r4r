# Restful-API for RDF data (r4r)

If you have data described in [RDF](https://www.w3.org/TR/WD-rdf-syntax-971002/) format (e.g. a knowledge base or an ontology) and you want to publish them on the web following the REST principles via API over HTTP,  this is your site!

You only need [Docker](https://docs.docker.com/install/) installed in your system.

Once Docker is installed, you should be able to run the R4R container by:

```
docker run -it --rm  \
    -p 8080:7777  \
    -v "$(pwd)/resources:/resources" \
    -e "SPARQL_ENDPOINT=https://dbpedia.org/sparql" \
    -e "RESOURCE_NAMESPACE=https://dbpedia.org/resource/" \
    cbadenes/r4r:latest
```

The `-v` parameter sets a local folder where the resources that are published in the API are defined.

The `-e` parameters will be seen below.

The `-p` parameter defines the port where the service will be listening. In this case we've set 8080, so let's go to [http://localhost:8080/](http://localhost:8080/) from a web browser and see a welcome message like this:

```
  Welcome to R4R ;)

```

A folder named `resources` should have been created in the same directory where you launched the container. This folder will contain the definition of the resources that will be published through the API.

In order to continue with the following steps, it is recommended to use a text editor such as [Atom](https://atom.io) ( with [velocity](https://atom.io/packages/atom-language-velocity), [json](https://atom.io/packages/pretty-json) and [sparql](https://atom.io/packages/language-sparql) plugins), to easily handle JSON and Sparql files.

## Static Resources

Creates the file `resources/movies/get.json.vm` to handle a HTTP_GET request by providing the following content:

```json
[
 {
   "name":"movie1",
   "uri" : "uri1"
 },
 {
    "name":"movie2",
    "uri" : "uri2"
 }
]

```

The request to [http://localhost:8080/movies](http://localhost:8080/movies) returns that json.

You have easily created a MOCK server!

## Dynamic Resources

The `-e` parameters set the endpoint (`SPARQL_ENDPOINT`) and namespace (`RESOURCE_NAMESPACE`) where the service retrieves the data through Sparql queries.

To retrieve data from a HTTP_GET request, simply create the file `resources/movies/get.sparql` with the following content:


```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ?m_uri ?m_name
WHERE {
    ?m_uri rdf:type dbo:Film ;        
        foaf:name ?m_name .
    FILTER (lang(?m_name) = 'en') .    
}
```

R4R allows query results (e.g. `m_uri` and `m_name`) to be available in the json template.

How? Easily, we can edit the file `get.json.vm` to use the sparql query responses using [Velocity Template Language](https://velocity.apache.org/engine/1.7/user-guide.html#velocity-template-language-vtl-an-introduction):

```
[
    #foreach( $movie in $results )
        {
            "uri" : "$movie.m_uri",
            "name" : "$movie.m_name"
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]
```

A new variable named `results` is always available from this template. It has all values retrieved in the sparql query so can be iterated to create a list of resources. In our example, a list of movies is create with two fields: `uri` and `name`.

Now, a different json message is returned by doing the request [http://localhost:8080/movies](http://localhost:8080/movies)

## Paginated Query

R4R allows you to make paginated queries by simply adding the query param `size` and `offset` since they are special variables.

If we want the list of only 5 films, it will be enough to request it this way: [http://localhost:8080/movies?size=5](http://localhost:8080/movies?size=5)

and if we want the next page, enough with:  [http://localhost:8080/movies?size=5&offset=1](http://localhost:8080/movies?size=5&offset=1)

When considering paginated queries it is necessary to set the `ORDER` option in the Sparql query.





## Optional fields

Some fields may not always be available. For example, the `bonusTracks` field is accessible only in some movies.

Let's set it into the sparql query (`resources/movies/get.sparql`) by:

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ?m_uri ?m_name ?m_bonus
WHERE {
    ?m_uri rdf:type dbo:Film ;
        foaf:name ?m_name .
    FILTER (lang(?m_name) = 'en') .
    OPTIONAL { ?m_uri dbp:bonusTracks ?m_bonus } .
}
```

And we also load it into the JSON template (`resources/movies/get.json.vm`)to handle this conditional value:

```
[
    #foreach( $movie in $results )
        {
            "uri" : "$movie.m_uri",
            #if ($movie.m_bonus)
             "bonus": "$movie.m_bonus",
            #end
            "name" : "$movie.m_name"
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]
```

The returned json only includes the `bonus` field when it has value by doing a request to [http://localhost:8080/movies](http://localhost:8080/movies).

## Dynamic Fields

New fields can easily be generated without the need to request new information.

These fields are created from existing information, for example to indicate the **ID** of a resource from its URI.

It would be enough to add the necessary operations in the JSON generation template (`resources/movies/get.json.vm`) as follows:

```
[
    #foreach( $movie in $results )
        #set ( $index = $movie.m_uri.lastIndexOf("/") )
        #set ( $index = $index + 1)
        #set ( $id = $movie.m_uri.substring($index, $movie.m_uri.length()))
        {
            "uri" : "$movie.m_uri",
            "id"  : "$id",
            #if ($movie.m_bonus)
             "bonus": "$movie.m_bonus",
            #end
            "name" : "$movie.m_name"
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]
```


These fields are available at: [http://localhost:8080/movies](http://localhost:8080/movies)

## Query Parameters

To filter by movie name, for example, just add the following condition to the sparql query (`resources/movies/get.sparql`):

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ?m_uri ?m_name ?m_bonus
WHERE {
    ?m_uri rdf:type dbo:Film ;
        foaf:name ?m_name .
    FILTER (lang(?m_name) = 'en') .
    OPTIONAL { ?m_uri dbp:bonusTracks ?m_bonus } .
    FILTER ( isNumeric(?name) = True || regex(?m_name, ?name, "i") ) .
}
```

Now you can make requests like this: [http://localhost:8080/movies?name=Games](http://localhost:8080/movies?name=Games)

Be careful when naming variables, because if you use the same name in the query field as the variable returned in the sparql query an error will occur.

### Operand Data Types

The XSD type of the query parameter can be specified by adding the following suffixes to its name:
- `_dt`: xsd:dateTime
- `_d`: xsd:double
- `_i`: xsd:integer
- `_s`: xsd:string
- `_b`: xsd:boolean


## Sort Criteria

The `sort` query param establishes the order of a solution sequence. 

It contains a field name and an order modifier (either `+` or `-`). Each ordering comparator is either ascending (indicated by the `+ modifier or by no modifier) or descending (indicated by the `-` modifier).

Internally, R4R adds an ORDER BY clause to the sparql query with the closest property (by using the Levenhstein distance) to the one specified in the `sort` field.

Now you can make requests like this: http://localhost:8080/movies?name=Games&sort=-name

## Query Path

In order to recover the information of a specific resource it is enough to add the following files:

The `resources/movies/getById.sparql` file with the following content:

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ( ?id AS ?m_uri ) ?d_name ?m_country ?m_name ?m_abstract ?m_budget ?m_released
WHERE {
	?id dbo:director ?d_uri .
	?d_uri foaf:name ?d_name .
	OPTIONAL {?id dbp:country ?m_country} .
	OPTIONAL {?id dbo:budget ?m_budget} .
	OPTIONAL {?id foaf:name ?m_name} .
	OPTIONAL {?id dbp:released ?m_released} .
	OPTIONAL {?id dbo:abstract ?m_abstract . FILTER (lang(?m_abstract) = 'en')} .
}
```

A variable `?sid` is also available with a short version of the id (i.e without the namespace)

And the `resources/movies/getById.json.vm` with this content:

```
{
    "uri" : "$m_uri",
    "director" : "$d_name",
    #if ($m_country)
      "country" : "$m_country",
    #end
    #if ($m_wiki)
      "wiki" : "$m_wiki",
    #end
    #if ($m_abstract)
      "abstract" : "$m_abstract",
    #end
    #if ($m_budget)
      "budget" : "$m_budget",
    #end
    #if ($m_released)
      "released" : "$m_released",
    #end
    "title": "$m_name"
}

```

Now, you can get details about a movie by: [http://localhost:8080/movies/WarGames](http://localhost:8080/movies/WarGames)

#### Nested URIs

Sometimes the type of the resource is required to identify it, and adding the ID to the namespace is not enough:
    
    
    https://eu.dbpedia.org/movies/WarGames
     

In this scenario, R4R should be run with the environment variable `RESOURCE_NESTED=True`. In this way, the resource type is incorporated, together with the namespace and ID, to create its URI.


## Related Resources

To request resources from a given one, simply add a **subfolder** with the template files.

For instance, to get the list of starring characters in the film it is enough to create the following files:

- `resources/movies/characters/get.sparql`:

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX dbr: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?name ?gender ( ?starring AS ?uri )
WHERE {
	?id dbo:starring ?starring .
	?starring foaf:name ?name .
	?starring foaf:gender ?gender .
	OPTIONAL {?name rdfs:label ?string . FILTER (lang(?string) = 'en') }
}
```

- `resources/movies/characters/get.json.vm`:

```
[
    #foreach( $person in $results )
     {
        "uri" : "$person.uri",
        "name" : "$person.name",
        "gender" : "$person.gender"
     }
     #if ( $velocityCount < ${results.size()} )
        ,
     #end
    #end
]
```

That list can then be obtained by [http://localhost:8080/movies/WarGames/characters](http://localhost:8080/movies/WarGames/characters)


## Security

A HTTP basic authentication can be defined by only adding a (list of) `user:password` pair(s) in the environment variable `API_USERS` as follows:

```
docker run -it --rm  \
    -p 8080:7777  \
    -v "$(pwd)/resources:/resources" \
    -e "SPARQL_ENDPOINT=http://dbpedia.org/sparql" \
    -e "RESOURCE_NAMESPACE=http://dbpedia.org/resource/" \
    -e "API_USERS=user1:pwd1;user2:pwd2" \
    cbadenes/r4r:latest
```

Now, the request to [http://localhost:8080/movies](http://localhost:8080/movies) require a user name ( e.g `user1`) and a password (e.g `pwd1`) to be performed. These values have been defined in that environment variable.

```sh
  curl -u user1:pwd1 http://localhost:8080/movies
```

## Documentation

Static HTML can be used to document API Rest. All files in `resources/doc` folder are available from a browser.

A Swagger interface can be created by describing our services in a YAML file, as follows:

```yaml
swagger: '2.0'
info:
  description: API documentation.
  version: 1.0.0
  title: Swagger DBpedia Movies
  termsOfService: 'http://swagger.io/terms/'
  contact:
    email: cbadenes@fi.upm.es
  license:
    name: Apache 2.0
    url: 'http://www.apache.org/licenses/LICENSE-2.0.html'
host: localhost:8080
basePath: /
schemes:
  - http
paths:
  '/movies':
    get:
      summary: Gets a list of movies
      operationId: getMovies
      parameters:
        - name: size
          in: query
          description: number of movie to return
          required: false
          type: integer
        - name: offset
          in: query
          description: page of movies to return
          required: false
          type: integer  
      responses:
        '200':
          description: OK
          schema:
            type: array
            items:
              type: object
              properties:
                uri:
                  type: string
                id:
                  type: string
                bonus:
                  type: string
                name:
                  type: string                  
  '/movies/{id}':
    get:
      summary: Find movie by ID
      description: Returns a single movie
      operationId: getMovieById
      produces:
        - application/json
      parameters:
        - name: id
          in: path
          description: ID of movie to return
          required: true
          type: string
      responses:
        '200':
          description: successful operation
          schema:
            $ref: '#/definitions/Movie'
        '400':
          description: Invalid ID supplied
        '404':
          description: Movie not found
definitions:
  Movie:
    type: object
    required:
      - uri
      - director
      - country
    properties:
      uri:
        type: string
      director:
        type: string
      title:
        type: string
      budget:
        type: integer
      country:
        type: string
      wiki:
        type: string
      abstract:
        type: string
      released:
        type: string
```

Then, a static html description can be created from that description in [swagger editor](http://editor.swagger.io/) by selecting `Generate Client > html2` option .

A new file (`index.html`) is created and would be placed into the `resources/doc` folder. In this way, our API is described in: [http://localhost:8080/doc/index.html](http://localhost:8080/doc/index.html).   


# Collaborative development

It can be extended with [webhook](https://github.com/adnanh/webhook) to easily create HTTP endpoints (hooks) on your server, which you can use to execute configured commands.


For example, if you're using Github, you can use it to set up a hook that updates the resources for your R4R project on your staging server, whenever you push changes to the master branch of your project.

It would be enough to create the `hooks.json` file:

```sh
[
  {
    "id": "update",
    "execute-command": "/home/cbadenes/project/hook-git.sh",
    "command-working-directory": ""
  }
]
```

And a script to run it from `nohup`:

```sh
nohup webhook -hooks hooks.json -verbose > nohup.log 2>&1 &
echo $! > nohup.pid
tail -f nohup.log
```


Then, the configured command can be something like this `hook-git.sh`:

```sh
#!/bin/bash
echo "Updating content"
git pull origin master
```

# Full Deployment

Docker-compose allows to start R4R and Virtuoso together in one command line.

Describe both services in a `docker-compose.yml` as follows:

```yaml
version: '3'

services:
  r4r:
    image: cbadenes/r4r
    container_name: r4r
    environment:
      SPARQL_ENDPOINT: "http://virtuoso:8890/sparql"
      RESOURCE_NAMESPACE: "http://www.example.com"
    volumes:
      - ./resources:/resources
    ports:
     - "8080:7777"
    depends_on:
     - "virtuoso"
  virtuoso:
    image: tenforce/virtuoso
    container_name: virtuoso
    environment:
      SPARQL_UPDATE: "true"
      DBA_PASSWORD: "my-pass"
      DEFAULT_GRAPH: "http://www.example.com/my-graph"
    volumes:
      - ./data:/data
    ports:
      - "8890:8890"

```

Then, run it by: `$ docker-compose up`

Virtuoso will be available at: [http://localhost:8890](http://localhost:8890), and R4R will be listening at: [http://localhost:8080](http://localhost:8080)

# Acknowledgments

This research was supported by the Spanish national project Datos 4.0, and by the European Union's Horizon 2020 research and innovation programme under grant agreement No 780247: [TheyBuyForYou](http://theybuyforyou.eu).
