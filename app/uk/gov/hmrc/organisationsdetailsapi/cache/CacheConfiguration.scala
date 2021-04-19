package uk.gov.hmrc.organisationsdetailsapi.cache

import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class CacheConfiguration @Inject()(configuration: Configuration) {
  lazy val cacheEnabled = configuration
    .getOptional[Boolean](
      "cacheV1.enabled"
    )
    .getOrElse(true)

  lazy val cacheTtl = configuration
    .getOptional[Int](
      "cacheV1.ttlInSeconds"
    )
    .getOrElse(60 * 15)

  lazy val collName = configuration
    .getOptional[String](
      "cacheV1.collName"
    )
    .getOrElse("individuals-employments-v1-cache")

  lazy val saKey = configuration
    .getOptional[String](
      "cacheV1.saKey"
    )
    .getOrElse("sa-income")

  lazy val payeKey = configuration
    .getOptional[String](
      "cacheV1.payeKey"
    )
    .getOrElse("paye-income")
}
