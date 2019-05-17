# Restful-API for RDF data (r4r)

If you have data described in [RDF](https://www.w3.org/TR/WD-rdf-syntax-971002/) format (e.g. a knowledge base or an ontology) and you want to publish them on the web following the REST principles via API over HTTP,  this is your site! 

You only need [Docker](https://docs.docker.com/install/) installed in your system. 

Once Docker is installed, you should be able to run the R4R container by:

```
docker run -it --rm  \
    -p 8080:7777  \
    -v "$(pwd)/resources:/resources" \
    -e "SPARQL_ENDPOINT=http://dbpedia.org/sparql" \
    -e "RESOURCE_NAMESPACE=http://dbpedia.org/resource/" \
    cbadenes/r4r:latest
```

The `-v` parameter sets a local folder where the resources that are published in the API are defined. 

The `-p` parameter defines the port where the service will be listening. In this case we've set 8080, so let's go to [http://localhost:8181/](http://localhost:8080/) from a web browser and see a welcome message like this: 

```
  Welcome to R4R ;)
  
```

## Static Resources

Creates a file  named `get.json.vm` in folder `resources/movies` with the following contents:

```json
[
 {
   "name":"movie1"
 },
 {
    "name":"movie2"
 }
]

```

The request to [http://localhost:8080/movies](http://localhost:8080/movies) returns that json.

You have easily created a MOCK server! 

## Dynamic Resources

The `-e` parameters set the endpoint (`SPARQL_ENDPOINT`) and namespace (`RESOURCE_NAMESPACE`) where the resources to be published in the service are hosted. 

The service retrieves the data through a Sparql query described in file `get.sparql` located in `resources/movies`, like this one:

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT DISTINCT ?uri ?name ?budget
WHERE {
    ?uri rdf:type dbo:Film .
    OPTIONAL { ?uri dbo:budget ?budget } .
    OPTIONAL { ?uri foaf:name ?name . FILTER (lang(?name) = 'en') } .    
}
```

R4R allows query results (e.g. `uri`, `name` and `budget`) to be available in the json template. 

How? Easily, we can edit the file `get.json.vm` to use the sparql query responses: 

```
[
    #foreach( $movie in $results )
        {
            "uri" : "$movie.uri",
            "name" : "$movie.name",        
            "budget" : $movie.budget
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]


```

A new variable named `results` is available from this template. It has all values retrieved in the sparql query. We can iterate on this variable to create a list of movies with three fields: `uri` , `name` and `budget`.

Now a json message with a list of ten movies is returned by doing the request [http://localhost:8080/movies](http://localhost:8080/movies)

## Optional fields

Some fields may not always be available. For example, the `bonusTracks` field is accessible only in some movies. 

Let's set it into the sparql query (`resources/movies/get.sparql`):

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT DISTINCT ?uri ?name ?budget ?bonus
WHERE {
    ?uri rdf:type dbo:Film .
    OPTIONAL { ?uri dbo:budget ?budget } .
    OPTIONAL { ?uri dbp:bonusTracks ?bonus } .
    OPTIONAL { ?uri foaf:name ?name . FILTER (lang(?name) = 'en') } .
}

```

And we also load it into the JSON template (`resources/movies/get.json.vm`)to handle this conditional value:

```
[
    #foreach( $movie in $results )
        {
            "uri" : "$movie.uri",
            #if ($movie.bonus)
            "bonus": "$movie.bonus",
            #end
            #if ($movie.bonus)
            "budget" : $movie.budget,
            #end
            "name" : "$movie.name"
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]


```

The returned json only includes the `bonus` field when it has value by doing a request to [http://localhost:8080/movies](http://localhost:8080/movies).

## Query Parameters

To filter by movie name, for example, just add the following condition to the sparql query (`resources/movies/get.sparql`):

```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT DISTINCT ?uri ?name ?budget ?bonus
WHERE {
    ?uri rdf:type dbo:Film .
    OPTIONAL { ?uri dbo:budget ?budget } .
    OPTIONAL { ?uri dbp:bonusTracks ?bonus } .
    OPTIONAL { ?uri foaf:name ?name . FILTER (lang(?name) = 'en') } .
    FILTER ( regex(?name, ?title, "i") || isLiteral(?title) = False ) .
}

```


Now you can make requests like this: [http://localhost:8080/movies?title=WarGames](http://localhost:8080/movies?title=WarGames)

Be careful when naming variables, because if you use the same name in the query field as the variable returned in the sparql query an error will occur (e.g. `name`).

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
SELECT ?director ?country ( ?id AS ?uri )
WHERE {
	?id dbo:director ?duri .
	?duri foaf:name ?director .
	?id dbp:country ?country .
	?id dbp:language ?language .
}
```


And the `resources/movies/getById.json.vm` with this content:

```
{
    "uri" : "$uri",
    "director" : "$director",
    "country" : "$country"
}
```

Now, you can get details about a movie by: [http://localhost:8080/movies/WarGames](http://localhost:8080/movies/WarGames)


## Dynamic Fields

New fields can easily be generated without the need to request new information. 

These fields are created from existing information, for example to indicate the **ID** of a resource from its URI.

It would be enough to add the necessary operations in the JSON generation template (`resources/movies/get.json.vm`) , such as these:

```
[
    #foreach( $movie in $results )
        #set ( $index = $movie.uri.lastIndexOf("/") )
        #set ( $index = $index + 1)
        #set ( $id = $movie.uri.substring($index, $movie.uri.length()))
        {
            "uri" : "$movie.uri",
            "id"  : "$id",
            #if ($movie.bonus)
            "bonus": "$movie.bonus",
            #end
            #if ($movie.bonus)
            "budget" : $movie.budget,
            #end
            "name" : "$movie.name"
         }
         #if ( $velocityCount < ${results.size()} )
            ,
         #end
    #end
]

```


These fields are available at: [http://localhost:8080/movies](http://localhost:8080/movies)


## Inner Resources

To request resources from a given one, simply add a subfolder with the template files. 
 
For instance, to get the actors and actresses protagonists of a film it is enough to create the following files: 
 
- `resources/movies/roles/get.sparql`: 

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

- `resources/movies/roles/get.json.vm`:
 
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

Then that list can be obtained by [http://localhost:8080/movies/WarGames/roles](http://localhost:8080/movies/WarGames/roles)


## Paginated Query

R4R allows you to make paginated queries by simply adding the query param `size` and `offset`. They are special variables. 


If we want the list of only 5 films, it will be enough to request it this way: [http://localhost:8080/movies?size=5](http://localhost:8080/movies?size=5)
 
and if we want the next page, enough with:  [http://localhost:8080/movies?size=5&offset=1](http://localhost:8080/movies?size=5&offset=1)