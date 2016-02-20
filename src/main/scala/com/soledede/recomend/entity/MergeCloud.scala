package com.soledede.recomend.entity

import org.apache.commons.lang.builder.{ToStringStyle, ToStringBuilder}

/**
  * Created by soledede on 16/2/18.
  */
class MergeCloud(var id: String = "113567",
                 var sku: String = "MAD443",
                 var title: String = "防护口罩",
                 var original: String = "9001",
                 var price: Double = 12345.4,
                 var picUrl: String = "xxx/xxx.jpg|ttt/xxx/s.jpg|oo/se/x.png",
                 var bigPicUrl: String = "sxx.jpg",
                 var leadTime: String = "现货或5天内",
                 var warehouse: String = "C1",
                 var salesUnit: String = "盒",
                 var minOrder: String = "1盒(1盒=50袋)",
                 var cnstatus: Int = 1,
                 var productPages: String = "A7-303|A8-168",
                 var category1: String = "防护工具",
                 var category2: String = "呼吸防护",
                 var category3: String = "防护口罩",
                 var category4: String = "呼吸口罩",
                 var categoryId1: Int = 1003332,
                 var categoryId2: Int = 1000434,
                 var categoryId3: Int = 1003435,
                 var categoryId4: Int = 1003437,
                 var series: String = "3M耳带式",
                 var category: String = "3M口罩",
                 var categoryId: Int = 1004533,
                 var industry: String = "医疗",
                 var brandId: Int = 1421,
                 var brandZh: String = "3M",
                 var brandEn: String = "3M",
                 var brandPicUrl: String = "ssss/sss/s.jpg",
                 var keywords: String = "",
                 var tags: String = "医疗,3M,口罩",
                 var location: String = "40.715,-74.007",
                 var popularity: Float = 0.6f,
                 var clickRate: Float = 0.9f,
                 var sales: Int = 10000,
                 var salesMonth: Int = 50,
                 var createDate: Long = System.currentTimeMillis(),
                 var updateDate: Long = System.currentTimeMillis(),
                 var cityId: String = "145_234_456",
                 var isRestrictedArea: Int = 0,
                 var deliveryTime: Float = 5f,
                 var t635_tf: Float = 2.5f,
                 var t89_s: String = "Honeywell"
                ) extends Serializable {
  override def toString(): String = {
    ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE)
  }
}

object testMergeCloud {
  def main(args: Array[String]) {
    println(new MergeCloud().toString)
  }
}
