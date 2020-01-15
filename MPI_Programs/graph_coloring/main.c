#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef long long ll;
int adj[100][100];
int c[100];
int cc[100];
int rands[100];
int gen_count(int n)
{
    int cnt=0;
    int i;
    for(i=0;i<n;i++)
    {
        if(c[i]==-1)
            cnt++;
    }
    return cnt;
}
int main(int argc,char *argv[])
{
    MPI_Init(NULL, NULL);

    // Get the number of processes
    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    // Get the rank of the process
    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    // Get the name of the processor
    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int name_len;
    MPI_Get_processor_name(processor_name, &name_len);
    //int n=atoi(argv[1]);
    //printf("n is %d\n",n);
    //int elems_per_process = n/world_size;
    //printf("elems is %d\n",elems_per_process);
    int i;
    int n,m,elems_per_process;
    if(world_rank==0)
    {
        memset(adj,-1,sizeof(adj));
        memset(c,-1,sizeof(c));
        //int n,m;
        scanf("%d %d",&n,&m);
        elems_per_process = n/world_size;
        
        for(i=0;i<m;i++)
        {
            int n1,n2;
            scanf("%d %d",&n1,&n2);
            adj[n1][n2]=1;
            adj[n2][n1]=1;
        }
        for(i=0;i<m;i++)
        {

            rands[i]=rand();
            printf("%d %d\n",i,rands[i]);
        }

    }
    MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(&m,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(&elems_per_process,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(adj,100*100,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(rands,100,MPI_INT,0,MPI_COMM_WORLD);
    int min_col=0;




    while(1)
    {


        
        MPI_Bcast(c,100,MPI_INT,0,MPI_COMM_WORLD);
        if(gen_count(n)==0)
            break;
        /*
        printf("elems is %d\n",elems_per_process);
        if(world_rank==1)
        {
            printf("edge 0->1 %d\n",adj[0][1]);
            printf("color 1%d\n",c[1]);
        }
        */
        for(i=0;i<elems_per_process;i++)
        {
            int ind=world_rank*elems_per_process +i;
            int j;
            int flag=0;
            if(c[ind]!=-1)
                continue;
            for(j=0;j<n;j++)
            {

                if(adj[ind][j]!=-1 && rands[ind]<rands[j] && c[j]==-1)
                {

                    flag=1;
                }

            }
            if(flag==0)
            {
                //printf("ind is %d %d\n",ind,i);
                c[i]=min_col;
            }
        }
        MPI_Gather(c,elems_per_process, MPI_INT,cc,elems_per_process, MPI_INT, 0,MPI_COMM_WORLD);
        if(world_rank==0)
        {
            for(i=0;i<n;i++)
            {
                if(c[i]==-1)
                    c[i]=cc[i];
            }
        }
        min_col++;

        if(world_rank==0)
        {
            for(i=0;i<n;i++)
            {
                printf("%d\n",c[i]);
            }
            printf("\n");
        }

        //break;
    }

    MPI_Finalize();
    
    return 0;
}
