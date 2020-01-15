#include<bits/stdc++.h>
#include<mpi.h>
using namespace std;
double A[1005][1005];
double A_sub[1005][1005];
double A_copy[1005][1005];
double B_copy[1005];
double B_sub[1005];
double B[1005];
double x[1005];
int pivot[1005];
int haspivoted[1005];
double maxx[1005];
double max2[1005];
double for_copy[1005];
double tmp;
void print(int n)
{
	for(int i=0;i<n;i++)
	{
		for(int j=0;j<n;j++)
		{
			cout<<A[i][j]<<" ";
		}
		cout<<endl;
	}
}
void print1(int n)
{
	for(int i=0;i<n;i++)
	{
		cout<<B[i]<<endl;
	}

}
void print2(int n)
{
	for(int i=0;i<n;i++)
	{
		cout<<x[i]<<endl;
	}

}
void swap(int i,int j,int n)
{
	for(int i1=0;i1<n;i1++)
	{
		for_copy[i1]=A[i][i1];
		A[i][i1]=A[j][i1];
		A[j][i1]=for_copy[i1];
	}
	tmp=B[i];
	B[i]=B[j];
	B[j]=tmp;
}
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
    int n;
    memset(pivot,-1,sizeof(pivot));
    memset(haspivoted,-1,sizeof(haspivoted));

    if(world_rank==0)
    {
		ifstream infile; 
		infile.open(argv[1]);
		infile >> n;
		infile >> n;
		n--;
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<n;j++)
			{
				infile>>A[i][j];
			}
			infile >> B[i];
		} 

		for(int i=0;i<n;i++)
		{
			x[i]=0;

		}
		//
	}
	MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
	MPI_Bcast(A,100*100,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(B,100,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(x,100,MPI_DOUBLE,0,MPI_COMM_WORLD);

	int num_rows=n/world_size;
	
	int cur_piv;
	//print1(n);
	for(int iters=0;iters<n-1;iters++)
	{


		//MPI_Gather(maxx,num_rows, MPI_DOUBLE,max2,num_rows, MPI_DOUBLE, 0,MPI_COMM_WORLD);
		if(world_rank==0)
		{
			double maxval=-10000000.0;
			cur_piv=-1;
			for(int i=iters;i<n;i++)
			{
				if(A[i][iters]>maxval)
				{
					maxval=A[i][iters];
					cur_piv=i;
				}
			}
			pivot[cur_piv]=iters;
			//cerr<<cur_piv<<" "<<iters<<endl;
			swap(cur_piv,iters,n);
			//cerr<<"Swap happened"<<endl;
			
		}
		cur_piv=iters;

		MPI_Bcast(A,1005*1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
		MPI_Bcast(B,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);

		for(int i=0;i<num_rows;i++)
		{
			int cur_row=world_rank*num_rows + i;
			if(cur_row>iters)
			{
				//A[cur_row][iters]+lam*A[cur_piv][iters]=0
				double lambda=-1.0*A[cur_row][iters]/A[cur_piv][iters];

				for(int k=0;k<n;k++)
				{

					/*if(cur_row==2 && cur_piv==1 && k==2)
					{
						cerr<<lambda<<"Debug "<<A[cur_piv][k]<<" "<<lambda*A[cur_piv][k]<<endl;
					}*/
					A[cur_row][k]+=lambda*A[cur_piv][k];
					
				}

				B[cur_row]+=lambda*B[cur_piv];

			}
			B_sub[i]=B[cur_row];
			for(int j=0;j<n;j++)
			{
				A_sub[i][j]=A[cur_row][j];
			}
			

		}

		//
		//
		MPI_Gather(A_sub,num_rows*1005, MPI_DOUBLE,A,num_rows*1005, MPI_DOUBLE, 0,MPI_COMM_WORLD);
		MPI_Gather(B_sub,num_rows, MPI_DOUBLE,B,num_rows, MPI_DOUBLE, 0,MPI_COMM_WORLD);

	}
	

	//Back Substituion
	if(world_rank==0)
	{

		for(int i=n-1;i>-1;i--)
		{
			double ans=B[i];
			for(int j=i+1;j<n;j++)
			{
				ans-=x[j]*A[i][j];
			}
			x[i]=ans/A[i][i];
			
		}
		print2(n);

	}
	
	MPI_Finalize();
	return 0;
}