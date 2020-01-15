#include<bits/stdc++.h>
#include<mpi.h>
using namespace std;
double A[1005][1005];
double B[1005];
double x[1005];
double r[1005];
double p[1005];
double d[1005];
double p1[1005];
double Ap[1005];
double mul(double a[],double b[],int n)
{
	double ans=0.0;
	for(int i=0;i<n;i++)
		ans+=a[i]*b[i];
	return ans;
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
			d[i]=0;
			r[i]=B[i];
			p[i]=B[i];

		}
		//
	}
	double rsold=mul(r,r,n);
	MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
	//MPI_Bcast(&rsold,1,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(A,1005*1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(B,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(x,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	//MPI_Bcast(r,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	MPI_Bcast(p,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	//MPI_Bcast(d,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);
	//float local_sum=par_multiply(A,B,n,world_size,world_rank);
	//float global_sum;
	//cout<<A[0][0]<<" "<<A[0][1]<<" "<<A[1][0]<<" "<<A[1][1]<<endl;
	int iters=n;
	int num_rows=n/world_size;
	double x1[1005];
	double x2[1005];
	double rsnew;
	memset(x2,-1,sizeof(x2));
	int tobreak=0;
	for(int its=0;its<iters;its++)
	{

		//Find Ap
		
		for(int i=0;i<num_rows;i++)
		{
			int cur_row=world_rank*num_rows + i;
			double ans=0.0;
			for(int j=0;j<n;j++)
			{
				ans+=A[cur_row][j]*p[j];
			}
			p1[i]=ans;
			
		}
		//Recv_x1
		MPI_Gather(p1,num_rows, MPI_DOUBLE,Ap,num_rows, MPI_DOUBLE, 0,MPI_COMM_WORLD);

		if(world_rank==0)
		{
			double alpha = rsold/(mul(p,Ap,n));
			//cerr<<"alpha "<<alpha<<endl;
			for(int i=0;i<n;i++)
			{
				x[i]+=alpha*p[i];
				r[i]-= alpha*Ap[i];
			}
			rsnew=mul(r,r,n);
			if( sqrt(rsnew)< 1e-10)
			{
				tobreak=1;
				
				
			}
			MPI_Bcast(&tobreak,1,MPI_INT,0,MPI_COMM_WORLD);
			if(tobreak==1)
				break;
			for(int i=0;i<n;i++)
			{
				p[i]=r[i]+ (rsnew/rsold)*p[i];
			}
			rsold=rsnew;
			/*
			for(int i=0;i<n;i++)
			{
				cout<<"it " <<its<<" x "<<i<<" " <<x[i]<<endl;
			}
			*/
		}
		if(world_rank!=0)
		{
			MPI_Bcast(&tobreak,1,MPI_INT,0,MPI_COMM_WORLD);
			if(tobreak!=0)
				break;
		}
		MPI_Bcast(p,1005,MPI_DOUBLE,0,MPI_COMM_WORLD);

	}
	if(world_rank==0)
	{
		for(int i=0;i<n;i++)
		{
			cout<<"final x "<<i<<" " <<x[i]<<endl;
		}
	}
	MPI_Finalize();

	return 0;
}
