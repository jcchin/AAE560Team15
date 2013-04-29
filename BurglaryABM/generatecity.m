%Anteland Citybuilder v1.0
%Output data columns are ID, X, Y, Population, Risk Level, Shoot Count, Drug Count
%Lowerleft position
Xstart=575000+rand*16000;
Ystart=634000+rand*16000;
%lay city
CellPos=zeros(73,86);
CrimeMat=zeros(73,86);
PopMat=zeros(73,86);
ShootMat=zeros(73,86);
DrugMat=zeros(73,86);
%lay blocks
for i=1:15
CellPos(i*5-4,:)=zeros(1,86)+1;
end
for i=1:17
CellPos(:,i*5-4)=zeros(73,1)+1;
end
k=1.85;
%Set Boundaries (Cut corners)
ul=tan(pi/4*rand+pi/4*rand);
ulc=[rand^(1/2)*7.3*k 86-rand^(1/2)*8.6*k];
ulb=ulc(2)-ul*ulc(1);
ur=-tan(pi/4*rand+pi/4*rand);
urc=[73-rand^(1/2)*7.3*k 86-rand^(1/2)*8.6*k];
urb=urc(2)-ur*urc(1);
lr=tan(pi/4*rand+pi/4*rand);
lrc=[73-(rand^(1/2))*7.3*k (rand^(1/2))*8.6*k];
lrb=lrc(2)-lr*lrc(1);
ll=-tan(pi/4*rand+pi/4*rand);
llc=[rand^(1/2)*7.3*k rand^(1/2)*8.6*k];
llb=llc(2)-ll*llc(1);
for i=1:73
for j=1:86
if not(((-i*ul+j)<ulb) && ((-i*ur+j)<urb) && ((-i*ll+j)>llb) && ((-i*lr+j)>lrb) )
CellPos(i,j)=0;
end
end
end
%Seed population centers
for i=1:17
x=floor(rand*73+1);
y=floor(rand*86+1);
m=12.5+(rand^(1/3))*12.5;
hx=0.0105+rand*0.004;
hy=0.0105+rand*0.004;
for j=1:73
12
for k=1:86
PopMat(j,k)=PopMat(j,k)+(0.70+0.60*rand)*m* ...
exp(-(0.80+0.40*rand)*(1.95*hx*((j-x)^2)^0.75+1.95*hy*((k-y)^2)^0.75));
end
end
end
PopMat=1.55+PopMat./5.4;
%Seed crime centers
for i=1:12
x=floor(rand*73+1);
y=floor(rand*86+1);
m=12.5+(rand^(1/3))*12.5;
hx=0.0135+rand*0.004;
hy=0.0135+rand*0.004;
for j=1:73
for k=1:86
CrimeMat(j,k)=CrimeMat(j,k)+0.83*(0.70+0.60*rand)*m* ...
exp(-(0.80+0.40*rand)*(1.95*hx*((j-x)^2)^0.75+1.95*hy*((k-y)^2)^0.75));
end
end
end
avg=sum(sum(CrimeMat))/73/86;
CrimeMat=CrimeMat.*(25/avg);
CrimeMat=CrimeMat.^0.5;
%Place population and crime on city blocks
for i=1:73
for j=1:86
PopMat(i,j)=floor(PopMat(i,j)*CellPos(i,j));
CrimeMat(i,j)=CrimeMat(i,j)*CellPos(i,j);
end
end
%prepare Result Matrix
clear CellData;
colheaders={'ID', 'X', 'Y', 'Population', 'Risk Level', 'Shoot Count', 'Drug Count'};
%Combine data into Result Matrix
k=0;
for i=1:73
for j=1:86
if (CellPos(i,j)>0)
k=k+1;
ShootMat(i,j)=floor((rand^7.5)*(3800+CrimeMat(i,j)^4)/3726);
DrugMat(i,j)=floor((rand^5)*(1100+CrimeMat(i,j)^4)/1070);
CellData(k,:)=[k-1 i*100+Xstart j*100+Ystart PopMat(i,j) ...
CrimeMat(i,j) ShootMat(i,j) DrugMat(i,j)];
end
end
end
population=sum(sum(PopMat))
13
%Graph City
figure('Name','Population Distribution')
imagesc(PopMat);
figure('Name','Risk Levels')
imagesc(CrimeMat);
figure('Name','Shoot Counts')
imagesc(ShootMat);
figure('Name','Drug Counts')
imagesc(DrugMat);
%Place data into "data" for CrimeGame analysis
clear data;
data=CellData;