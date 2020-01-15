#include<stdio.h>
#include<mpi.h>
#include<string.h>
#include<stdlib.h>

int final[100];
int ptrs[100];
int c[100];
int cmpfunc (const void * a, const void * b) //what is it returning?
{
   return ( *(int*)a - *(int*)b ); //What is a and b?
}

void merge(int elems_per_process,int n,int num_processes)
{
	int i;
	for(i=0;i<num_processes;i++)
	{
		ptrs[i]=i*elems_per_process;
	}
	int done=0;
	int min,idx;
	while(done<n)
	{
		min=1000000;
		idx=-1;
		for(i=0;i<num_processes;i++)
		{
			if(ptrs[i]<(i+1)*elems_per_process)
			{
				if(c[ptrs[i]]<min)
				{
					min=c[ptrs[i]];
					idx=i;
				}
			}
		}
		final[done]=min;
		ptrs[idx]++;
		done++;
	}
	return;
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
    int n=atoi(argv[1]);
    //printf("n is %d\n",n);
    int elems_per_process = n/world_size;
    //printf("elems is %d\n",elems_per_process);
    int a[100];
    int b[100];
    if(world_rank==0)
    {
    	int i;
    	for(i=0;i<n;i++)
    	{
    		scanf("%d",&a[i]);
    	}
       
    }
    MPI_Scatter(a, elems_per_process, MPI_INT,b,
            elems_per_process, MPI_INT, 0, MPI_COMM_WORLD);

    /*if(world_rank==1)
    {
	int i;
	for(i=0;i<elems_per_process;i++)
	{
		printf("%d\n",b[i]);
	}
    }*/
    qsort(b,elems_per_process,sizeof(int),cmpfunc);
   /* if(world_rank==1)
    {
	int i;
	for(i=0;i<elems_per_process;i++)
	{
		printf("%d\n",b[i]);
	}
    }*/
    MPI_Gather(b,elems_per_process, MPI_INT,c,elems_per_process, MPI_INT, 0,
           MPI_COMM_WORLD);

    if(world_rank==0)
    {
    	merge(elems_per_process,n,world_size);
    	for(int i=0;i<n;i++)
    	{
    		printf("%d\n",final[i]);
    	}
    }
    MPI_Finalize();
    return 0;

}
