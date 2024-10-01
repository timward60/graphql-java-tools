package graphql.kickstart.tools.resolver

import graphql.kickstart.tools.*
import graphql.kickstart.tools.GenericType
import graphql.kickstart.tools.ResolverError
import graphql.kickstart.tools.ResolverInfo
import graphql.kickstart.tools.TypeClassMatcher
import graphql.kickstart.tools.util.JavaType
import graphql.language.FieldDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

/**
 * @author Andrew Potter
 */
internal abstract class FieldResolver(
    val field: FieldDefinition,
    val search: FieldResolverScanner.Search,
    val options: SchemaParserOptions,
    relativeTo: JavaType
) {
    val resolverInfo: ResolverInfo = search.resolverInfo
    val genericType = GenericType(search.type, options).relativeToPotentialParent(relativeTo)

    abstract fun scanForMatches(): List<TypeClassMatcher.PotentialMatch>

    abstract fun createDataFetcher(): DataFetcher<*>

    /**
     * Add source resolver depending on whether or not this is a resolver method
     */
    protected fun createSourceResolver(): SourceResolver {
        return if (this.search.source != null) {
            SourceResolver { _, _ -> this.search.source }
        } else {
            SourceResolver { environment, sourceObject ->
                val source = if (sourceObject != null) {
                    // if source object is known, environment is null as an optimization (LightDataFetcher)
                    sourceObject
                } else {
                    environment
                        ?: throw ResolverError("Expected DataFetchingEnvironment to not be null!")
                    environment.getSource<Any>()
                        ?: throw ResolverError("Expected source object to not be null!")
                }

                if (!this.genericType.isAssignableFrom(source.javaClass)) {
                    throw ResolverError("Expected source object to be an instance of '${this.genericType.getRawClass().name}' but instead got '${source.javaClass.name}'")
                }

                source
            }
        }
    }
}

fun interface SourceResolver {

    fun resolve(environment: DataFetchingEnvironment?, source: Any?): Any
}
