package voznytsia.lab6;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final String INPUT_FILENAME = "src\\main\\java\\voznytsia\\lab6\\input.txt";
    private static final String OUTPUT_FILENAME = "src\\main\\java\\voznytsia\\lab6\\output.txt";

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Operations operations = new Operations();

        try {
            String inputFilePath = new File(INPUT_FILENAME).getAbsolutePath();
            Scanner scanner = new Scanner(new File(inputFilePath));

            int sizeMM = scanner.nextInt();
            int sizeME = scanner.nextInt();
            int sizeMX = scanner.nextInt();
            int sizeB = scanner.nextInt();
            int sizeD = scanner.nextInt();

            double q = scanner.nextDouble();
            scanner.close();

//            double[][] MM = operations.generateRandomMatrix(sizeMM, sizeMM);
//            double[][] ME = operations.generateRandomMatrix(sizeME, sizeME);
//            double[][] MX = operations.generateRandomMatrix(sizeMX, sizeMX);
//            double[] B = operations.generateRandomArray(sizeB);
//            double[] D = operations.generateRandomArray(sizeD);
            double[][] MM = new double[sizeMM][sizeMM];
            double[][] ME = new double[sizeME][sizeME];
            double[][] MX = new double[sizeMX][sizeMX];
            double[] B = new double[sizeB];
            double[] D = new double[sizeD];

            operations.readMatrix("MM.txt", MM);
            operations.readMatrix("ME.txt", ME);
            operations.readMatrix("MX.txt", MX);
            operations.readVector("B.txt", B);
            operations.readVector("D.txt", D);
//            operations.readMatrix("MMBig.txt", MM);
//            operations.readMatrix("MEBig.txt", ME);
//            operations.readMatrix("MXBig.txt", MX);
//            operations.readVector("BBig.txt", B);
//            operations.readVector("DBig.txt", D);

//            operations.writeArrayToFile(MM, "MM.txt");
//            operations.writeArrayToFile(ME, "ME.txt");
//            operations.writeArrayToFile(MX, "MX.txt");
//            operations.writeArrayToFile(B, "B.txt");
//            operations.writeArrayToFile(D, "D.txt");
//            operations.writeArrayToFile(MM, "MMBig.txt");
//            operations.writeArrayToFile(ME, "MEBig.txt");
//            operations.writeArrayToFile(MX, "MXBig.txt");
//            operations.writeArrayToFile(B, "BBig.txt");
//            operations.writeArrayToFile(D, "DBig.txt");

            String outputFilePath = new File(OUTPUT_FILENAME).getAbsolutePath();
            PrintWriter writer = new PrintWriter(outputFilePath);

            ExecutorService executor = Executors.newFixedThreadPool(3);

            BlockingQueue<double[][]> queue1 = new ArrayBlockingQueue<>(1);
            BlockingQueue<double[][]> queue2 = new ArrayBlockingQueue<>(1);
            BlockingQueue<double[]> queue3 = new ArrayBlockingQueue<>(1);

            Callable<double[][]> task1 = () -> {
                double[][] r = operations.multiplyMatrix(MM, operations.subtractMatrix(ME, MX));
                try {
                    queue1.put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                System.out.println("\nResult 1: " + Arrays.deepToString(r));
                writer.println("\nResult 1: " + Arrays.deepToString(r));
                return r;
            };

            Callable<double[][]> task2 = () -> {
                double[][] r = operations.multiplyMatrixByScalar(operations.multiplyMatrix(ME, MX), q);
                try {
                    queue2.put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                System.out.println("\nResult 2: " + Arrays.deepToString(r));
                writer.println("\nResult 2: " + Arrays.deepToString(r));
                return r;
            };

            Callable<double[]> task3 = () -> {
                double[] r = operations.multiplyVectorByScalar(D, operations.findMinValue(B));
                try {
                    queue3.put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                System.out.println("\nResult 3: " + Arrays.toString(r));
                writer.println("\nResult 3: " + Arrays.toString(r));
                return r;
            };

            Future<double[][]> future1 = executor.submit(task1);
            Future<double[][]> future2 = executor.submit(task2);
            Future<double[]> future3 = executor.submit(task3);

            try {
                future1.get();
                future2.get();
                future3.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }

            double[][] result1 = queue1.take();
            double[][] result2 = queue2.take();
            double[] result3 = queue3.take();

            double[][] MA = new double[result1.length][result1[0].length];
            for (int i = 0; i < MA.length; i++) {
                for (int j = 0; j < MA[0].length; j++) {
                    MA[i][j] = result1[i][j] + result2[i][j];
                }
            }

            double[] Y = operations.multiplyVectorByMatrix(B, ME);
            for (int i = 0; i < Y.length; i++) {
                Y[i] = Y[i] + result3[i];
            }

            System.out.println("\nFinal Result: \nMA=" + Arrays.deepToString(MA) + "\n\nY=" + Arrays.toString(Y));
            writer.println("\nFinal Result: \nMA=" + Arrays.deepToString(MA) + "\n\nY=" + Arrays.toString(Y));

            long endTime = System.nanoTime();
            long resultTime = (endTime - startTime);
            System.out.println("\nDuration: " + resultTime + " ns");
            writer.println("\nDuration: " + resultTime + " ns");
            writer.close();

//            operations.printMatrix(MM);
//            operations.printMatrix(ME);
//            operations.printMatrix(MX);
//            for (double d : B) {
//                System.out.print(d + " ");
//            }
//            System.out.println("\n");
//            for (double d : D) {
//                System.out.print(d + " ");
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
