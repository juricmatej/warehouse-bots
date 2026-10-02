import mpi.MPI;


public  class  Main {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);


        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            //gazda
            Master.go(size - 1);
        } else {
            //bot
            BotMain.go(rank);
        }

        MPI.Finalize();
    }
}