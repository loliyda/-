package KNN.classifiers;

import KNN.dataController.DataHandling;
import org.ejml.simple.SimpleMatrix;

import java.io.*;

/**
 * 实现knn分类器
 * @author sq
 * @version 1.0
 *
 */
public class KnnClassifier {
    SimpleMatrix model = null;
    private int K = 9;

    public KnnClassifier() {

    }//初始化分类器，选择邻居数量，要感兴趣可以提供选择距离方法，k近邻搜索方法

    /**
     *
     * @param k knn的邻居数量
     */
    public KnnClassifier(int k) {
        K = k;
    }

    /**
     * 存储训练集作为预测时计算距离的依据
     *@param train_x SimpleMatrix类型的特征矩阵x
     *@param train_y SimpleMatrix类型的01标签矩阵y
     *
     */
    public void fit(SimpleMatrix train_x, SimpleMatrix train_y) {
        model = train_x.concatColumns(train_y);
    }

    /**
     *用于从文件加载knn模型
     * @param fileName 模型所在路径
     *
     */
    public void fitFromFile(String fileName) {
        //从指定文件名里加载模型
        FileReader fr = null;
        try {
            fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            K = Integer.parseInt(line);
            //System.out.println(K);
            line=br.readLine();
            String[] temp=line.split(",");
            int numCol=Integer.parseInt(temp[0]);
            int numRow=Integer.parseInt(temp[1]);
            SimpleMatrix modelFromFile=new SimpleMatrix(numRow,numCol);
            for(int i=0;i<numRow;i++)
            {
                line = br.readLine();
                temp=line.split(",");
                for(int j=0;j<numCol;j++)
                    modelFromFile.set(i,j,Double.parseDouble(temp[j]));
            }
            //System.out.println(temp);
            br.close();
            fr.close();
            model=modelFromFile;
//            System.out.println(model);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     *将模型（用于后续计算距离的点集）永久化存储
     * @param fileName 要存储的模型文件路径
     *
     */
    public void storeModule(String fileName) {
        //以模型名命名文件名，将模型永久化存储
        String[][] tempStrw = new String[model.numRows()][model.numCols()];
        //System.out.println(model);
        for (int i = 0; i < model.numRows(); i++) {
            for (int j = 0; j < model.numCols(); j++) {
                tempStrw[i][j] = "" + model.get(i, j);
            }
        }

        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write("");
            writer.write(String.valueOf(K) + '\n');
            writer.write(model.numCols()+","+model.numRows()+"\n");
            for (int i = 0; i < model.numRows(); i++) {
                for (int j = 0; j < model.numCols(); j++) {
                    writer.write(tempStrw[i][j] + ",");
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     *
     * @param test_x_ 用于预测的特征矩阵x
     * @return 返回预测出来的SimpleMatrix类型的01标签向量
     */
    public SimpleMatrix predict(SimpleMatrix test_x_) {

//        SimpleMatrix data = dataHanding.loadMatrixFromCsv(filemane);
        //double []pre_type = new double[data.numRows()];
        double[] pre_type = new double[test_x_.numRows()];
        //用来训练的数据
//        HashMap<String,SimpleMatrix> hashMap = dataHanding.loadMatrixFromCsv_xy
//                ("C:\\Users\\WL173\\Desktop\\Datasets\\AEEEM\\csv\\PDE.csv");

        SimpleMatrix x = DataHandling.min_max_handing(model.cols(0, model.numCols() - 1)), y = model.cols(model.numCols() - 1, model.numCols());
        double[][] x_min_max_double = new double[x.numRows()][x.numCols()];
        for (int i = 0; i < x.numRows(); i++) {
            for (int j = 0; j < x.numCols(); j++) {
                x_min_max_double[i][j] = x.get(i, j);
            }
        }
        double[] y_tag = new double[y.numRows()];
        for (int i = 0; i < y.numRows(); i++) {
            y_tag[i] = y.get(i, 0);
        }
        //对需要预测数据的预处理
        SimpleMatrix test_x = test_x_.cols(0, test_x_.numCols());
        //SimpleMatrix test_y = test_x_.cols(test_x_.numCols()-1,test_x_.numCols());
        SimpleMatrix test_x_min_max = DataHandling.min_max_handing(test_x);
        double[][] test_x_min_max_double = new double[test_x_min_max.numRows()][test_x_min_max.numCols()];
        for (int i = 0; i < test_x_min_max.numRows(); i++) {
            for (int j = 0; j < test_x_min_max.numCols(); j++) {
                test_x_min_max_double[i][j] = test_x_min_max.get(i, j);
            }
        }
        //储存标记
//        int[]true_y = new int[test_x_.numRows()];
//        for(int i = 0;i<true_y.length;i++){
//            true_y[i] = (int)test_y.get(i,0);
//        }
        int[][] min_index = new int[test_x_min_max_double.length][K];//记录各组需要预测数据的前K近下标
        /*
        Knn，核心过程
         */
        for (int i = 0; i < test_x_min_max_double.length; i++) {
            double[] distance = new double[x_min_max_double.length];//保存个点距离
            for (int j = 0; j < x_min_max_double.length; j++) {
                distance[j] = cal_distance(x_min_max_double[j], test_x_min_max_double[i]);
            }
            int[] temp = find_k_index(distance, K);
            for (int k = 0; k < K; k++) {
                min_index[i][k] = temp[k];
            }
        }
        for (int i = 0; i < min_index.length; i++) {
            int count = 0;
            for (int j = 0; j < K; j++) {
                if (y_tag[min_index[i][j]] == 1.0) {
                    count++;
                }
            }
            if (count > K / 2) {
                pre_type[i] = 1;
            }
        }
        return new SimpleMatrix(pre_type.length, 1, true, pre_type);
    }
    private int[] find_k_index(double[] distinceArray, int K) {
        int[] min_index = new int[K];
        int[] index = new int[distinceArray.length];
        for (int i = 0; i < distinceArray.length; i++) index[i] = i;//初始化下标数组
        //冒泡排序，并且用index数组记录下标
        if (distinceArray != null && distinceArray.length > 0) {
            for (int i = 0; i < distinceArray.length; i++) {
                for (int j = i + 1; j < distinceArray.length; j++) {
                    if (distinceArray[j] < distinceArray[i]) {
                        double temp = distinceArray[j];
                        distinceArray[j] = distinceArray[i];
                        distinceArray[i] = temp;

                        int temp_ = index[j];
                        index[j] = index[i];
                        index[i] = temp_;
                    }
                }
            }
        } else {
            System.out.println("排序数组为空");
        }
        for (int i = 0; i < K; i++) min_index[i] = index[i];
        return min_index;
    }
    //计算临近距离[除开求解分类]
    private double cal_distance(double[] paraFirstData, double[] paraSecondData) {
        double tempDistince = 0;
        if ((paraFirstData != null && paraSecondData != null) && paraFirstData.length == paraSecondData.length) {
            for (int i = 0; i < paraFirstData.length - 1; i++) {
                tempDistince += (paraFirstData[i] - paraSecondData[i]) * (paraFirstData[i] - paraSecondData[i]);
            }
        } else {
            System.out.println("firstData 与 secondData 数据结构不一致");
        }
        return tempDistince;
    }
}
