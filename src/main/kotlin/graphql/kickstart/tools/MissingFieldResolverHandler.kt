package graphql.kickstart.tools

import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher

interface MissingFieldResolverHandler {
    fun handle(field: FieldDefinition, type: ObjectTypeDefinition,
                               options: SchemaParserOptions): DataFetcher<Any?> {
        return options.missingResolverDataFetcher ?: DataFetcher<Any?> { TODO("Schema resolver not implemented") }
    }
}