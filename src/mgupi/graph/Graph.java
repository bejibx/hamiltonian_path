package mgupi.graph;

import java.io.*;
import java.util.*;

public class Graph {

    /*
    * INNER CLASSES **************************************************************
    * */

     class Edge {
         Vertex mFrom;
         Vertex mTo;
         int mID = 0;

         public Edge(int ID, Vertex from, Vertex to) {
             mID = ID;
             mFrom = from;
             mTo = to;
         }

         public Vertex getDestination() {
             return mTo;
         }

         public Vertex getSource() {
             return mFrom;
         }

         public int getID() {
             return mID;
         }
    }

    class Vertex implements Comparable {
        int mID = 0;

        ArrayList<Edge> mOutputEdges = new ArrayList<Edge>();
        ArrayList<Edge> mInputEdges = new ArrayList<Edge>();

        public Vertex(int ID) {
            mID = ID;
        }

        public int getID() {
            return mID;
        }

        public int outEdgesCount() {
            return mOutputEdges.size();
        }

        public int inEdgesCount() {
            return mInputEdges.size();
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Vertex) {
                return Integer.compare(((Vertex)o).getID(), this.getID());
            } else
                return -1;
        }
    }

    private class HamiltonianPathFinder {

         private class Node {
             /**
              * Массив уже посещенных вершин
              */
            private TreeSet<Vertex> mPassedVertex = new TreeSet<Vertex>();

            public boolean isPassed(Vertex vertex) {
                return mPassedVertex.contains(vertex);
            }

            public void markAsPassed(Vertex vertex) {
                mPassedVertex.add(vertex);
            }
        }

        public class Path {
            private ArrayList<Vertex> mNodes = new ArrayList<Vertex>();
        }

        /**
         * Текущий путь.
         */
        private Path mPath = new Path();

        /**
         * Текущий путь в виде множетва на основе красно-чёрного дерева.
         * Позволяет за lgN проверить содержится ли элемент в текущем пути.
         */
        private TreeSet<Vertex> mPathSet = new TreeSet<Vertex>();

        /**
         * Массив посещенных вершин для каждого узла.
         */
        private ArrayList<Node> mAttendedVertex = new ArrayList<Node>();

        /**
         * Ссылка на текущий узел.
         */
        private Vertex mCurrent;

        /**
         * Ссылка на корневой узел (из которого начали обход).
         */
        private Vertex mRoot;

        /**
         * Текущая длинна пути - 1.
         */
        private int mCurrentIndex;

        /**
         * @return возвращает узел для текущего шага.
         */
        private Node getCurrentNode() {
            return mAttendedVertex.get(mCurrentIndex);
        }

        /**
         * Создает новый узел для переданной вершины.
         * @param vertex вершина, для которой необходимо создать узел пути.
         */
        private void stepToNextNode(Vertex vertex) {
            mPath.mNodes.add(vertex);
            mPathSet.add(vertex);
            getCurrentNode().markAsPassed(vertex);
            mAttendedVertex.add(new Node());
            mCurrent = vertex;
            mCurrentIndex++;
        }

        /**
         * Удаляет верхнюю вершину из пути и переходит к предыдущей.
         */
        private void returnToPrevNode() {
            mPath.mNodes.remove(mCurrentIndex);
            mPathSet.remove(mCurrent);
            mAttendedVertex.remove(mCurrentIndex);
            mCurrentIndex--;
            mCurrent = mPath.mNodes.get(mCurrentIndex);
        }

        /**
         * Возвращает для текущей вершины следующую не посещенную смежную вершину.
         */
        private Vertex getNextVertex() {
            for (Edge e : mCurrent.mOutputEdges) {
                Vertex adjustVertex = e.getDestination();
                if (!getCurrentNode().isPassed(adjustVertex) && !mPathSet.contains(adjustVertex)) {
                    return e.getDestination();
                }
            }
            return null;
        }

        /**
         * Конструктор.
         * @param root вершина, с которой будет начат обход графа.
         */
        public HamiltonianPathFinder(Vertex root) {
            mRoot = root;
            mPath.mNodes.add(root);
            mPathSet.add(root);
            mAttendedVertex.add(new Node());
            mCurrent = root;
            mCurrentIndex = 0;
        }

        public int[] find() {
            boolean inProcess = true;
            int[] result = null;

            /*
            * Метод перебора Робертса и Флореса
            * */
            do {
                Vertex nextVertex = getNextVertex();
                if (nextVertex != null) {
                    stepToNextNode(nextVertex);
                } else {

                    if (mCurrentIndex == 0) {
                        /*
                         * Если мы попали сюда, значит пройдены все возможные пути для корневой
                         * вершины и не найден гамильтонова цикла -> граф его не содержит.
                         * Вернём null.
                         */
                        inProcess = false;
                    } else if (mCurrentIndex == mVerticesCount - 1 && hasEdgeBetween(mCurrent, mRoot)) {
                        /*
                        * Если попали сюда, значит текущая длина пути равна количеству вершин в графе
                        * (пройдены все вершины) и существует путь из последней вершины пути в корневую ->
                        * -> гамильтонов цикл найден.
                        * */
                        result = new int[mVerticesCount];
                        for (int i = 0; i < mVerticesCount; i++) {
                            result[i] = mPath.mNodes.get(i).getID();
                        }
                        inProcess = false;
                    } else {
                        /*
                        * Перепробовали пути во все смежные с текущей вершины и не нашли гамильтонов цикл.
                        * Вернемся на шаг назад.
                        * */
                        returnToPrevNode();
                    }
                }
            } while (inProcess);

            return result;
        }
    }

    public List<Vertex> vertices() {
        return Collections.unmodifiableList(mVertices);
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(mEdges);
    }

    /*
    * DATA FIELDS ************************************************************
    * */
    /**
     * Массив вершин графа.
     */
    private ArrayList<Vertex> mVertices = new ArrayList<Vertex>();

    /**
     * Массив рёбер графа.
     */
    private ArrayList<Edge> mEdges = new ArrayList<Edge>();

    /**
     * Матрица смежности графа. Позволяет быстро ответить на вопрос
     * "Существует ли в графе ребро из вершины X в вершину Y?".
     */
    private boolean mAdjacencyMatrix[][];

    /**
     * Количество вершин графа.
     */
    private int mVerticesCount = 0;

    /*
    * PRIVATE METHODS ********************************************************
    * */

     private void setVertexCount(int count) throws IllegalArgumentException {
        if (count > 0) {
            mVertices.clear();
            for (int i = 0; i < count; i++) mVertices.add(new Vertex(i));
            mAdjacencyMatrix = new boolean[count][count];
            mVerticesCount = count;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private boolean hasPrerequisite() {
        for (Vertex v : mVertices) {
            if ( v.outEdgesCount() < 1 && v.inEdgesCount() < 1 ) {
                return false;
            }
        }
        return true;
    }

    /*
    * PUBLIC METHODS *********************************************************
    * */

    public boolean hasEdgeBetween(Vertex from, Vertex to) {
        int row = from.getID();
        int col = to.getID();
        if (row >= 0 && row < mVerticesCount && col >= 0 && col < mVerticesCount) {
            return mAdjacencyMatrix[row][col];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void addEdge(int from, int to) {
        if (from >= 0 && from < mVertices.size()) {
            if (to >= 0 && to < mVertices.size()) {
                Vertex pFrom = mVertices.get(from);
                Vertex pTo = mVertices.get(to);
                Edge newEdge = new Edge(mEdges.size(), pFrom, pTo);
                mEdges.add(newEdge);
                pFrom.mOutputEdges.add(newEdge);
                pTo.mInputEdges.add(newEdge);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void LoadFromFile(File file) throws IllegalArgumentException, InputMismatchException, FileNotFoundException {

        Scanner scanner = new Scanner(file);

        int vertexCount = scanner.nextInt();

        setVertexCount(vertexCount);

        for (int row = 0; row < vertexCount; row++) {
            for (int col = 0; col < vertexCount; col++) {
                boolean value = scanner.nextInt() != 0;
                mAdjacencyMatrix[row][col] = value;
                if (value) {
                    addEdge(row, col);
                }
            }
        }
    }

    public void print() {
        System.out.println("Матрица смежности:");
        for (boolean[] aMAdjacencyMatrix : mAdjacencyMatrix) {
            for (int col = 0; col < mAdjacencyMatrix[0].length; col++) {
                System.out.print(aMAdjacencyMatrix[col] ? " 1" : " 0");
            }
            System.out.println();
        }
    }

    public void findHamiltonianPath() {
        if ( hasPrerequisite() ) {
            int[] result = new HamiltonianPathFinder(mVertices.get(0)).find();
            if (result == null) {
                System.out.println("В графе отсутствует гамильтонов цикл.");
            } else {
                System.out.println("Гамильтонов цикл: ");
                for (int ID : result) {
                    System.out.print(ID + 1);
                    System.out.print(" -> ");
                }
                System.out.println(1);
            }
        } else {
            System.out.println("В графе отсутствует гамильтонов цикл, поскольку не выполняется необходимое условие существования гамильтонова цикла.");
        }
    }

}
