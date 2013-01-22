package mgupi.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import org.apache.commons.collections15.Transformer;

public class Main {

    public static BasicVisualizationServer<Integer, Integer> getGraphVisualisation(mgupi.graph.Graph graph) {
        Graph<Integer, Integer> view = new SparseMultigraph<Integer, Integer>();
        for (mgupi.graph.Graph.Vertex v : graph.vertices()) {
            view.addVertex(v.getID());
        }
        for (mgupi.graph.Graph.Edge e : graph.edges()) {
            view.addEdge(e.getID(), e.getSource().getID(), e.getDestination().getID(), EdgeType.DIRECTED);
        }
        Layout<Integer, Integer> layout = new CircleLayout<Integer, Integer>(view);
        layout.setSize(new Dimension(600, 600));
        BasicVisualizationServer<Integer, Integer> visualisation =
                new BasicVisualizationServer<Integer, Integer>(layout);
        visualisation.setPreferredSize(new Dimension(650, 650));
        visualisation.getRenderContext().setVertexFillPaintTransformer(new Transformer<Integer, Paint>() {
            @Override
            public Paint transform(Integer integer) {
                return Color.WHITE;
            }
        });
        visualisation.getRenderContext().setVertexShapeTransformer(new Transformer<Integer, Shape>() {
            @Override
            public Shape transform(Integer integer) {
                return new Ellipse2D.Double(-20, -20, 40, 40);
            }
        });
        visualisation.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Integer>());
        visualisation.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        return visualisation;
    }

    public static void main(String[] args) {
	    if (args.length > 0) {
            File f = new File(args[0]);
            if (f.exists()) {
                mgupi.graph.Graph graph = new mgupi.graph.Graph();
                try {
                    graph.LoadFromFile(f);
                } catch (Exception e) {
                    System.out.println("Произошла ошибка при загрузке графа из файла. Проверьте формат файла.");
                    return;
                }
                System.out.println("Граф успешно загружен.");

                JFrame mainFrame = new JFrame("Граф");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.add(getGraphVisualisation(graph));
                mainFrame.pack();
                mainFrame.setVisible(true);

                graph.print();
                graph.findHamiltonianPath();
            } else {
                System.out.println("Файл \"" + args[0] + "\" не существует.");
            }
        } else {
            System.out.println("Укажите в качестве параметра путь к файлу, содержащему матрицу смежности графа.");
        }
    }
}
