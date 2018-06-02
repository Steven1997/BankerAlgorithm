import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class BankerAlgorithm {
    static class State{
        private static int[] resource = new int[10]; //总资源向量
        private static int[] available = new int[10]; //目前可用资源向量
        private static int[][] claim = new int[10][10]; //总需求矩阵
        private static int[][] alloc = new int[10][10]; //目前已分配资源矩阵
        private static int[][] need = new int[10][10]; //仍需要资源矩阵
        private static int category;  //资源种类数
        private static int process; //进程总数
        private static ArrayList<Integer> runSequence = new ArrayList<Integer>(); //进程运行序列
        private static ArrayList<ArrayList<Integer>> arr = new ArrayList<ArrayList<Integer>>(); //保存所有安全的进程运行序列
        private static int[] currentavail; //工作向量
        private static boolean[] flag = new boolean[10]; //标记进程是否运行完
        
        public static void desc(int[] array) {
            for(int i = 0;i < State.category;i++) {
                System.out.print(array[i] + " ");
            }
            System.out.println();
        }
        
        public static void deepDesc(int[][] array) {
            for(int i = 0;i < State.process;i++) {
                for(int j = 0;j < State.category;j++) {
                    System.out.print(array[i][j] + " ");
                }
                System.out.println();
            }
        }

    }

    static void init() {
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream("input.txt"));
            State.category = input.nextInt();
            State.process = input.nextInt();
            for(int i = 0;i < State.category;i++) {
                State.resource[i] = input.nextInt();
            }
            for(int i = 0;i < State.category;i++) {
                State.available[i] = input.nextInt();
            }
            for(int i = 0;i < State.process;i++) {
                for(int j = 0;j < State.category;j++) {
                    State.claim[i][j] = input.nextInt();
                }
            }
            for(int i = 0;i < State.process;i++) {
                for(int j = 0;j < State.category;j++) {
                    State.alloc[i][j] = input.nextInt();
                    State.need[i][j] = State.claim[i][j] - State.alloc[i][j];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
    }

    static void print() { //显示各矩阵和向量的初始状态
        System.out.println("共有 " + State.process + " 个进程和 " + State.category + " 种资源");
        System.out.println();
        System.out.println("resource");
        State.desc(State.resource);
        System.out.println();
        System.out.println("available");
        State.desc(State.available);
        System.out.println();
        System.out.println("claim");
        State.deepDesc(State.claim);
        System.out.println();
        System.out.println("alloc");
        State.deepDesc(State.alloc);
        System.out.println();
        System.out.println("need");
        State.deepDesc(State.need);
        System.out.println();
    }
    private static boolean tryAllocate(int processId) { //尝试从第 processId 个进程开始寻找安全序列
        int idx = processId - 1;
        for (int i = 0; i < State.category; i++) {
            if(State.alloc[idx][i] + State.need[idx][i] > State.claim[idx][i])
                //如果该进程某个资源已分配的数量和请求的数量总和大于总需求数量，则该请求不合法
                return false;
            else if(State.need[idx][i] > State.available[i])
                //如果当前所有可用的资源都满足不了这个进程的需求，拒绝分配，挂起这个进程
                return false;
        }

        return findSafeSequence(1);
    }

    private static boolean findSafeSequence(int nowCnt) { //递归寻找安全序列
            boolean find = false;
            if(nowCnt == State.process){
                State.arr.add((ArrayList<Integer>) State.runSequence.clone());
                return true;
            }
            for(int i = 0;i < State.process;i++) {
                if(State.flag[i]){
                    int j;
                    for(j = 0;j < State.category;j++) {
                        if (State.need[i][j] > State.currentavail[j])
                            break;
                    }

                    if (j == State.category) {
                        State.runSequence.add(i + 1);
                        for(int k = 0;k < State.category;k++) {
                            State.currentavail[k] += State.alloc[i][k];
                        }
                        State.flag[i] = false;
                        find = findSafeSequence(nowCnt + 1);
                        State.flag[i] = true;
                        for(int k = 0;k < State.category;k++) {
                            State.currentavail[k] -= State.alloc[i][k];
                        }
                        int last = State.runSequence.size() - 1;
                        State.runSequence.remove(last);
                    }
                }
            }
            return find;

    }



    public static void main(String[] args) {
        init();
        print();
        boolean isSafe = false;
        for(int i = 1;i <= State.process;i++){
            State.currentavail = State.available.clone();
            for(int j = 0;j < State.process;j++) {
                State.flag[j] = true;
            }
            State.runSequence.add(i);
            for(int k = 0;k < State.category;k++) {
                State.currentavail[k] += State.alloc[i - 1][k];
            }
            State.flag[i - 1] = false;
            if (tryAllocate(i)) {
                isSafe = true;
            }
            State.flag[i - 1] = true;
            State.runSequence.remove(0);
            for(int k = 0;k < State.category;k++) {
                State.currentavail[k] -= State.alloc[i - 1][k];
            }
        }

        if (isSafe) {
            System.out.println("当前状态是安全的，安全序列为：");
            for (int i = 0;i < State.arr.size();i++) {
                ArrayList<Integer> list = State.arr.get(i);
                if(i > 0){
                    System.out.println("或");
                }
                System.out.println(list);
            }
            System.out.println();
        }
        else
            System.out.println("当前序列是不安全的，处于死锁状态！");
    }
}
