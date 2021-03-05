package test;


import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ItemCF {
	final static int RECOMMENDER_NUM = 3;//推荐物品的最大个数
	
	public static void main(String[] args) throws IOException, TasteException {
		String file = "src/main/resources/data/testCF.csv";
        DataModel model = new FileDataModel(new File(file));//数据模型
        ItemSimilarity item=new EuclideanDistanceSimilarity(model);//用户相识度算法
        Recommender r=new GenericItemBasedRecommender(model,item);//物品推荐算法
        LongPrimitiveIterator iter =model.getUserIDs();
        while(iter.hasNext()){
        	long uid=iter.nextLong();
        	List<RecommendedItem> list = r.recommend(uid, 1);
        	System.out.printf("uid:%s",uid);
        	for (RecommendedItem ritem : list) {
                System.out.printf("(%s,%f)", ritem.getItemID(), ritem.getValue());
            }
            System.out.println();
        }
	}
 
}
