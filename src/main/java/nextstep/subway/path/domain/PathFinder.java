package nextstep.subway.path.domain;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.path.dto.Path;
import nextstep.subway.station.domain.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PathFinder {

    private WeightedMultigraph<Station, SectionEdge> graph = new WeightedMultigraph<>(SectionEdge.class);
    private LineRepository lineRepository;

    @Autowired
    public PathFinder(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    public PathFinder(List<Line> lines) {
        enrollLinesInGraph(lines);
    }

    private void enrollLinesInGraph(List<Line> lines) {
        lines.forEach(line -> line.enrollIn(graph));
    }

    public Path getPath(Station source, Station target) {
        if (graph.vertexSet().isEmpty()) {
            renewGraph();
        }

        isSame(source, target);
        isExistent(source, target);

        DijkstraShortestPath<Station, SectionEdge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        GraphPath<Station, SectionEdge> path = dijkstraShortestPath.getPath(source, target);
        if (path == null) {
            throw new IllegalArgumentException("출발역과 도착역은 연결되어 있어야 합니다.");
        }

        return new Path(path.getVertexList(), (int) path.getWeight(), path.getEdgeList());
    }

    public void renewGraph() {
        graph = new WeightedMultigraph<>(SectionEdge.class);
        lineRepository.findAll()
                .forEach(line -> line.enrollIn(graph));
    }

    private void isSame(Station source, Station target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("출발역과 도착역은 달라야 합니다.");
        }
    }

    private void isExistent(Station source, Station target) {
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            throw new IllegalArgumentException("존재하지 않는 역입니다.");
        }
    }
}
