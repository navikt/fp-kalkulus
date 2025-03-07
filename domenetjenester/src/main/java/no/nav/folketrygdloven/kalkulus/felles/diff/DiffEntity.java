package no.nav.folketrygdloven.kalkulus.felles.diff;


public class DiffEntity {

    private TraverseGraph traverser;

    public DiffEntity(TraverseGraph traverser) {
        this.traverser = traverser;
    }

    public <V> DiffResult diff(V entity1, V entity2) {
        TraverseGraph.TraverseResult entity1Result = traverser.traverse(entity1);
        TraverseGraph.TraverseResult entity2Result = traverser.traverse(entity2);

        return new DiffResult(this.traverser, entity1Result, entity2Result);
    }

    public <V> boolean areDifferent(V entity1, V entity2) {
        return !diff(entity1, entity2).getLeafDifferences().isEmpty();
    }

}
