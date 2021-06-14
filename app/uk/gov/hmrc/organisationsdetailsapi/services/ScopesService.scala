/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.organisationsdetailsapi.services

import play.api.Configuration
import uk.gov.hmrc.organisationsdetailsapi.config.{ApiConfig, EndpointConfig}

import javax.inject.Inject

class ScopesService @Inject()(configuration: Configuration) {

  private[services] lazy val apiConfig =
    configuration.get[ApiConfig]("api-config")

  private[services] def getScopeItemsKeys(scope: String): List[String] =
    apiConfig
      .getScope(scope)
      .map(s => s.fields)
      .getOrElse(List())

  def getScopeItems(scope: String): List[String] =
    getScopeItemsKeys(scope)
      .flatMap(fieldId => apiConfig.endpoints.flatMap(e => e.fields.get(fieldId)))

  def getFilterKeysForScope(scope: String): List[String] =
    apiConfig.getScope(scope).map(s => s.filters).getOrElse(List())

  def getEndpointFieldKeys(endpointKey: String): Iterable[String] =
    apiConfig
      .getEndpoint(endpointKey)
      .map(endpoint => endpoint.fields.keys.toList.sorted)
      .getOrElse(List())

  def getFieldNames(keys: Iterable[String]): Iterable[String] =
    apiConfig.endpoints
      .map(e => e.fields)
      .flatMap(value => keys.map(value.get))
      .flatten

  def getFilters(keys: Iterable[String]): Iterable[String] =
    apiConfig.endpoints
      .map(e => e.filters)
      .flatMap(value => keys.map(value.get))
      .flatten

  def getAllScopes: List[String] = apiConfig.scopes.map(_.name).sorted

  def getValidItemsFor(scopes: Iterable[String], endpoint: String): Iterable[String] = {
    val uniqueDataFields = scopes.flatMap(getScopeItemsKeys).toList.distinct
    val endpointDataItems = getEndpointFieldKeys(endpoint).toSet
    val authorizedDataItemsOnEndpoint = uniqueDataFields.filter(endpointDataItems.contains)
    getFieldNames(authorizedDataItemsOnEndpoint)
  }

  def getValidFilterKeys(scopes: Iterable[String],
                         endpoints: List[String]): Iterable[String] = {
    val endpointDataItems = endpoints.flatMap(e => getEndpointFieldKeys(e).toSet)
    val filtersForEndpoints = scopes.flatMap(getFilterKeysForScope)
    filtersForEndpoints.filter(endpointDataItems.contains)
  }

  def getValidFilters(scopes: Iterable[String],
                      endpoints: List[String]): Iterable[String] = {
    val endpointDataItems = endpoints.flatMap(e => getEndpointFieldKeys(e).toSet)
    val filtersForEndpoints = scopes.flatMap(getFilterKeysForScope).toSet
    val authorizedDataItemsOnEndpoint = filtersForEndpoints.filter(endpointDataItems.contains)
    getFilters(authorizedDataItemsOnEndpoint)
  }

  def getFilterToken(scopes: List[String], endpoint: String): Map[String, String] = {
    val regex = "<([a-zA-Z]*)>".r("token")
    def getTokenFromText(filterText:String):Option[String]  = regex.findFirstMatchIn(filterText).map(m => m.group("token"))
    def getFilterText(filterKey:String):Option[String] = apiConfig.getEndpoint(endpoint).flatMap(c => c.filters.get(filterKey))
    val filterKeys = getValidFilterKeys(scopes, List(endpoint))
    filterKeys.flatMap(key => getFilterText(key).flatMap(getTokenFromText).map(token => (key, token)).toList).toMap
  }

  def getValidItemsFor(scopes: Iterable[String], endpoints: List[String]): Set[String] = {
    val uniqueDataFields = scopes.flatMap(getScopeItemsKeys).toList.distinct
    val endpointDataItems = endpoints.flatMap(e => getEndpointFieldKeys(e).toSet)
    val authorizedDataItemsOnEndpoint = uniqueDataFields.filter(endpointDataItems.contains)
    getFieldNames(authorizedDataItemsOnEndpoint).toSet
  }

  def getValidFieldsForCacheKey(scopes: List[String]): String =
    scopes.flatMap(getScopeItemsKeys).distinct.reduce(_ + _)

  def getAccessibleEndpoints(scopes: Iterable[String]): Iterable[String] = {
    val scopeKeys = scopes.flatMap(s => getScopeItemsKeys(s)).toSeq
    apiConfig.endpoints
      .filter(endpoint => endpoint.fields.keySet.exists(scopeKeys.contains))
      .map(endpoint => endpoint.name)
  }

  def getEndpointLink(endpoint: String): Option[String] =
    apiConfig.getEndpoint(endpoint).map(c => c.link)

  def getEndpoints(scopes: Iterable[String]): Iterable[EndpointConfig] =
    getAccessibleEndpoints(scopes)
      .flatMap(endpoint => apiConfig.getEndpoint(endpoint))

  def getEndPointScopes(endpointKey: String): Iterable[String] = {
    val keys = apiConfig
      .getEndpoint(endpointKey)
      .map(endpoint => endpoint.fields.keys.toList.sorted)
      .getOrElse(List())

    apiConfig.scopes
      .filter(
        s => s.fields.toSet.intersect(keys.toSet).nonEmpty
      )
      .map(
        s => s.name
      )
      .sorted
  }
}

