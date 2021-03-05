package test;


import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.*;
import org.apache.mahout.cf.taste.impl.neighborhood.*;
import org.apache.mahout.cf.taste.impl.recommender.*;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.*;
import org.apache.mahout.cf.taste.recommender.*;
import org.apache.mahout.cf.taste.similarity.*;

import java.io.File;
import java.io.IOException;
import java.util.List;


/*
 * 
 * 
 * 基于用户的 CF（User CF）

基于用户的 CF 的基本思想相当简单，
基于用户对物品的偏好找到相邻邻居用户，
然后将邻居用户喜欢的推荐给当前用户。
计算上，就是将一个用户对所有物品的偏好作为一个向量来计算用户之间的相似度，
找到 K 邻居后，根据邻居的相似度权重以及他们对物品的偏好，
预测当前用户没有偏好的未涉及物品，计算得到一个排序的物品列表作为推荐。
图 2 给出了一个例子，对于用户 A，根据用户的历史偏好，
这里只计算得到一个邻居 - 用户 C，然后将用户 C 喜欢的物品 D 推荐给用户 A

 * 
 * 
 */
public class UserCF {
 
    final static int NEIGHBORHOOD_NUM = 2;//临近的用户个数
    final static int RECOMMENDER_NUM = 3;//推荐物品的最大个数
 
    public static void main(String[] args) throws IOException, TasteException {
        String file = "src/main/resources/data/testCF.csv";
        DataModel model = new FileDataModel(new File(file));//数据模型
        UserSimilarity user = new EuclideanDistanceSimilarity(model);//用户相识度算法,欧氏距离
        NearestNUserNeighborhood neighbor = new NearestNUserNeighborhood(NEIGHBORHOOD_NUM, user, model);//选择近邻算法
        //用户近邻算法
        Recommender r = new GenericUserBasedRecommender(model, neighbor, user);//用户推荐算法
        LongPrimitiveIterator iter = model.getUserIDs();///得到用户ID
 
        while (iter.hasNext()) {
            long uid = iter.nextLong();
            List<RecommendedItem> list = r.recommend(uid, RECOMMENDER_NUM);
            System.out.printf("-------------------uid:%s", uid);
            for (RecommendedItem ritem : list) {
                System.out.printf("==================(%s,%f)", ritem.getItemID(), ritem.getValue());
            }
            System.out.println();
        }
    }
} 
