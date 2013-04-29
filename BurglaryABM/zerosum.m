%input city data into "data" with columns "ID X Y Population Risk
%Level Shoot Count Drug Count"
%CombinedCells is combined cell data with "ID X Y Population CellCount
%Avg Risk Level Total Shoot Count Total Drug Count" as Colheaders

%Answer is ZeroSum Game result with ID, X, Y, Police, Crime
%rskLvlMat is Reward Matrix for Police with Police choosing Rows
%set m for number of 100feet per cell,
%p for average probability for police to prevent crime

%Process XY Data
m=5; %5 per block
p=0.75;
X=data(:,2);
Y=data(:,3);
XStart=min(X);
YStart=min(Y);
X=(X-XStart)./100;
Y=(Y-YStart)./100;
ydim=(floor(max(Y)/m)+1);
xdim=(floor(max(X)/m)+1);

%Set up Combined Data
RegionsLocs=zeros(xdim*ydim,3);
RegionsData=zeros(xdim*ydim,5);

%Combined XY coordinates
k=0;
for i=1:xdim
for j=1:ydim
k=k+1;
RegionsLocs(k,:)=[k-1 (i*m-m/2)*100+XStart (j*m-m/2)*100+YStart];
end
end

%Combine rest of the data
for i=1:size(data,1)
RegionsData(floor(X(i)/m)*ydim + floor(Y(i)/m) + 1,:)= ...
RegionsData(floor(X(i)/m)*ydim + floor(Y(i)/m) + 1,:)+horzcat([1] , data(i,4:7));
end
clear CombinedCells;
CombinedCells=horzcat(RegionsLocs,RegionsData);
i=0;
while (i<size(CombinedCells,1))
i=i+1;

%Remove Empty Regions
while (i<=size(CombinedCells,1) && CombinedCells(i,4)==0)
CombinedCells(i,:)= [ ];
end

%Average risk level and assign IDs
if (i<=size(CombinedCells,1))
10
CombinedCells(i,6)=CombinedCells(i,6)/CombinedCells(i,4);
CombinedCells(i,1)=i-1;
end
end
m=size(CombinedCells,1);

%Apply game
%Calculate New Risk Levels
shootdrugRatio=sum(CombinedCells(:,7))/sum(CombinedCells(:,8));

%Use 3x Avg RskLvl + shootings + drugcount*shootdrugRatio
NewRskLvl=CombinedCells(:,6).*3+CombinedCells(:,7)+CombinedCells(:,8).*shootdrugRatio;

%Build Reward Matrix
denom=sum(CombinedCells(:,5))/m; %Make p based on population --> 0: p=1, avg: p=preset p.
A=transpose(NewRskLvl).*-1;
rskLvlMat=A;
rskLvlMat(1,1)=rskLvlMat(1,1)*(1-2*(p^(CombinedCells(1,5)/denom)));
for i=2:m
rskLvlMat=vertcat(rskLvlMat,A);
rskLvlMat(i,i)=rskLvlMat(i,i)*(1-2*(p^(CombinedCells(i,5)/denom)));
end

%Solve Zerosum game
A=rskLvlMat.*100;
[m,n]=size(A);
X_a=linprog(-[1;zeros(m,1)],[ones(n,1) -A'], ...
zeros(n,1),[0 ones(1,m)],[1],[-inf;zeros(m,1)]);X_a(1,:)=[];
X_b=linprog([1;zeros(n,1)],[-ones(m,1) A], ...
zeros(m,1),[0 ones(1,n)],[1],[-inf;zeros(n,1)]);X_b(1,:)=[];
C=roundn(X_a,-6);
D=roundn(X_b,-6);
Answer=horzcat(CombinedCells(:,1:3),C,D);