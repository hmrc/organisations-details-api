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

package uk.gov.hmrc.organisationsdetailsapi.config

import com.typesafe.config.Config
import play.api.ConfigLoader
import uk.gov.hmrc.organisationsdetailsapi.services.PathTree

import scala.collection.JavaConverters._

case class ApiConfig(scopes: List[ScopeConfig], endpoints: List[EndpointConfig]) {

  def getScope(scope: String): Option[ScopeConfig] =
    scopes.find(c => c.name == scope)

  def getEndpoint(endpoint: String): Option[EndpointConfig] =
    endpoints.find(e => e.name == endpoint)

}

case class ScopeConfig(name: String,
                       fields: List[String],
                       filters: List[String] = List()) {}

case class EndpointConfig(name: String,
                          link: String,
                          title: String,
                          fields: Map[String, String],
                          filters: Map[String, String] = Map())

object ApiConfig {

  implicit val configLoader: ConfigLoader[ApiConfig] = (rootConfig: Config, path: String) => {

    val config = rootConfig.getConfig(path)

    def parseConfig(path: String): PathTree = {
      val keys: List[String] = config
        .getConfig(path)
        .entrySet()
        .asScala
        .map(x => x.getKey.replaceAllLiterally("\"", ""))
        .toList

      PathTree(keys, "\\.")
    }

    val endpointTree = parseConfig("endpoints")
    val endpointConfig: List[EndpointConfig] = endpointTree.listChildren
      .flatMap(
        key =>
          endpointTree
            .getChild(key)
            .flatMap(node => node.getChild("fields"))
            .map(node =>
              EndpointConfig(
                name = key,
                link = config.getString(s"endpoints.$key.endpoint"),
                title = config.getString(s"endpoints.$key.title"),
                fields = node.listChildren.toList.sorted
                  .map(field => (field, config.getString(s"endpoints.$key.fields.$field")))
                  .toMap,
                filters = node.listChildren.toList
                  .filter(field =>
                    config.hasPath(s"endpoints.$key.filters.$field"))
                  .sorted
                  .map(field =>
                    (field,
                      config.getString(s"endpoints.$key.filters.$field")))
                  .toMap
              )))
      .toList

    val scopeTree = parseConfig("scopes")
    val scopeConfig = scopeTree.listChildren
      .map(key =>
        ScopeConfig(
          name = key,
          fields = config
            .getStringList(s"""scopes."$key".fields""")
            .asScala
            .toList,
          filters =
            if (config.hasPath(s"""scopes."$key".filters"""))
              config
                .getStringList(s"""scopes."$key".filters""")
                .asScala
                .toList else List()))
      .toList

    ApiConfig(
      scopes = scopeConfig,
      endpoints = endpointConfig
    )
  }
}

