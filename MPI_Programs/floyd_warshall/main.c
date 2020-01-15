#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef long long ll;
int adj[100][100];
int dist[100][100];
int copy_dist[100][100];
int slave_dist[100][100];
#define INF 1000000007

int main(int argc,char* argv[])
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
    int n,m;
    int i,j,k;
    int elems_per_process;
    int source;
    if(world_rank==0)
    {
    	memset(adj,-1,sizeof(adj));
    	scanf("%d %d",&n,&m,&source);
    	elems_per_process=n/world_size;
    	for(i=0;i<m;i++)
    	{
    		int n1,n2,w;
    		scanf("%d %d %d",&n1,&n2,&w);
    		adj[n1][n2]=w;
    		adj[n2][n1]=w;
    	}
    	for(i=0;i<n;i++)
    	{
    		for(j=0;j<n;j++)
    		{
    			if(i==j)
                {
    				dist[i][j]=0;
                }
                else if(adj[i][j]>0)
                {
                    dist[i][j]=adj[i][j];
                }
                else
                {
                    dist[i][j]=INF;
                    //printf("%d\n",INF);
                }

    		}

    	}
    }

    MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(&m,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(&elems_per_process,1,MPI_INT,0,MPI_COMM_WORLD);
    MPI_Bcast(adj,100*100,MPI_INT,0,MPI_COMM_WORLD);
    if(world_rank==0)
    {
        ;
        //printf("broadcasts done\n");
    }

    for(k=0;k<n;k++)
    {
            for(i=0;i<n;i++)
            {
                for(j=0;j<n;j++)
                    slave_dist[i][j]=INF;
            }
    	MPI_Bcast(dist,100*100,MPI_INT,0,MPI_COMM_WORLD);
    	for(i=0;i<elems_per_process;i++)
    	{
    		int ind = world_rank*elems_per_process +i;
    		for(j=0;j<n;j++)
    		{
    			if(dist[ind][k]+dist[k][j]<dist[ind][j])
    			{
    				slave_dist[i][j]=dist[ind][k]+dist[k][j];
    			}
    		}
    	}
    	MPI_Gather(slave_dist,elems_per_process*100, MPI_INT,copy_dist,elems_per_process*100, MPI_INT, 0,MPI_COMM_WORLD);
    	if(world_rank==0)
    	{
        	for(i=0;i<n;i++)
        	{
        		for(j=0;j<n;j++)
        		{ 
                    
        			if(copy_dist[i][j]<dist[i][j])
                    {
        				dist[i][j]=copy_dist[i][j];
                        printf("%d %d %d\n",i,j,copy_dist[i][j]);
                        
                    }
        		}
        	}
            /*
            for(i=0;i<n;i++)
                printf("%d\n",dist[0][i]);
            
            */
            printf("\n");
        }
    }

    if(world_rank==0)
    {
        for(i=0;i<n;i++)
            printf("%d\n",dist[source][i]);
    }





    MPI_Finalize();
    
    return 0;
}
