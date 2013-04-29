clear all; %close all; clc
% pathname = '\\Client\C$\aae560\Results\18k_11_05_roadclosure_scnr2\';
pathname = '\\Client\C$\aae560\Results\18k_12_56\';
[id, coordx, coordy, security, value, numburg] =  textread([pathname 'results.csv'],'%s%f%f%s%f%f','delimiter',',','headerlines',1,'whitespace','');

bad_idx = find(abs(coordx) <= 0 & abs(coordy) <= 0);
id = removerows(id,bad_idx);
coordx = removerows(coordx,bad_idx);
coordy = removerows(coordy,bad_idx);
value = removerows(value,bad_idx);
numburg = removerows(numburg,bad_idx);

%% Plot Value Map
% 1234 x 950
% -110.9799 to -110.9175 = 5.06035395991787e-05
% 32.1976 to 32.2399 =  4.45110680321423e-05
% longsf = (max(coordy) - min(coordy))/950; %4.45110680321423e-05;
% latsf = (max(coordx) - min(coordx))/1234; %5.06035395991787e-05;
% maxvalue = max(250e3);
% SFx = 0.95;
% SFy = 0.98;
% 
% figure; hold on
% I = imread('\\Client\C$\aae560\burgleAnalyze.windows64\legend4.png');
% imshow(I); hold on
% cmap = colormap(jet(100));
% for i = 1:size(coordx,1)
%     clridx = cmap(min(max(1,ceil(value(i)/maxvalue*100)),100),:);
%     plot(SFx*(coordx(i)-min(coordx))/latsf+40,SFy*abs(((coordy(i)-min(coordy))/longsf)-950)+10,'d','color',clridx,'MarkerSize',3,'Linewidth',5);
% end
% axis tight
% title('Value Map')

%% Plot Number of Burglaries

% TODO: FIX THE READIN OF NUMBURG FOR SOME REASON ITS NOT READING IN PROPERLY
burg_idx = find(numburg > 0);
burg_south_tucson = find(coordx(burg_idx) <= -110.945 & coordy(burg_idx) <= 32.22);
burg_samhughes = find(coordx(burg_idx) >= -110.945);
burg_nw = find(coordx(burg_idx) <= -110.945 & coordy(burg_idx) >= 32.225);
burg_south_uni = find(coordx(burg_idx) <= -110.945 & coordx(burg_idx) >= -110.965...
                     & coordy(burg_idx) >= 32.22 & coordy(burg_idx) <= 32.23);
safe_idx = find(numburg <= 0);

Pct_in_south_tucson = length(burg_south_tucson)/length(burg_idx)
Pct_in_samhughes = length(burg_samhughes)/length(burg_idx)
Pct_in_nw = length(burg_nw)/length(burg_idx)
Pct_in_south_uni = length(burg_south_uni)/length(burg_idx)

figure; hold on;
plot(coordx(safe_idx),coordy(safe_idx),'bx');
plot(coordx(burg_idx),coordy(burg_idx),'ro','linewidth',5);
plot(coordx(burg_idx(burg_south_tucson)),coordy(burg_idx(burg_south_tucson)),'go','linewidth',5)
axis tight
title('Burgle Map')