function [out1,out2] = removerows(varargin)
%REMOVEROWS Remove matrix rows with specified indices.
%  
% <a href="matlab:doc removerows">removerows</a> processes input and target data by removing selected rows
% from the data.
%
% [Y,settings] = <a href="matlab:doc removerows">removerows</a>(X,'ind',rowIndices) takes matrix or cell data
% returns it with the specified rows removed, and also returns the settings
% used to perform the transform.
%
% Here random data is transformed by removing the second row.
%
%   x1 = [rand(1,20)*5-1; rand(1,20)*20-10; rand(1,20)-1];
%   [y1,settings] = <a href="matlab:doc removerows">removerows</a>(x1,'ind',2)
%
% <a href="matlab:doc removerows">removerows</a>('apply',X,settings) transforms X consistent with settings
% returned by a previous transformation.
%
%   x2 = [rand(1,20)*5-1; rand(1,20)*20-10; rand(1,20)-1];
%   y2 = <a href="matlab:doc removerows">removerows</a>('apply',x2,settings)
%
% <a href="matlab:doc removerows">removerows</a>('reverse',Y,settings) reverse transforms Y consistent with
% settings returned by a previous transformation.  When the removed rows
% are replace they are filled in with NaN values indicating their actual
% values are not known.
%
%   x1_again = <a href="matlab:doc removerows">removerows</a>('reverse',y1,settings)
%
% <a href="matlab:doc removerows">removerows</a>('dy_dx',X,Y,settings) returns the transformation derivative
% of Y with respect to X.
%
% <a href="matlab:doc removerows">removerows</a>('dx_dy',X,Y,settings) returns the reverse transformation
% derivative of X with respect to Y.
%
% See also REMOVECONSTANTROWS, FIXUNKNOWNS.

% Copyright 1992-2011 The MathWorks, Inc.
% $Revision: 1.1.6.12 $

%% =======================================================
%  BOILERPLATE_START
%  This code is the same for all Processing Functions.
  
  persistent INFO;
  if isempty(INFO), INFO = get_info; end,
  if (nargin < 1), error(message('nnet:Args:NotEnough')); end
  in1 = varargin{1};
  if ischar(in1)
    switch (in1)
      
      case 'create'
        % this('create',x,param)
        [args,param] = nnparam.extract_param(varargin(2:end),INFO.defaultParam);
        [x,ii,jj,wasCell] = nncell2mat(args{1});
        [out1,out2] = create(x,param);
        if (wasCell), out1 = mat2cell(out1,ii,jj); end
        
      case 'apply'
        % this('apply',x,settings)
        out2 = varargin{3};
        if out2.no_change
          out1 = varargin{2};
        else
          [in2,ii,jj,wasCell] = nncell2mat(varargin{2});
          out1 = apply(in2,out2);
          if (wasCell), out1 = mat2cell(out1,ii,jj); end
        end
        
      case 'reverse'
        % this('reverse',y,settings)
        out2 = varargin{3};
        if out2.no_change
          out1 = varargin{2};
        else
          [in2,ii,jj,wasCell] = nncell2mat(varargin{2});
          out1 = reverse(in2,out2);
          if (wasCell), out1 = mat2cell(out1,ii,jj); end
        end
        
      case 'dy_dx'
        % this('dy_dx',x,y,settings)
        out1 = dy_dx(varargin{2:4});
        
      case 'dy_dx_num'
        % this('dy_dx_num',x,y,settings)
        out1 = dy_dx_num(varargin{2:4});
        
      case 'dx_dy'
        % this('dx_dy',x,y,settings)
        out1 = dx_dy(varargin{2:4});
        
      case 'dx_dy_num'
        % this('dx_dy_num',x,y,settings)
        out1 = dx_dy_num(varargin{2:4});
        
      case 'info'
        % this('info')
        out1 = INFO;
        
      case 'check_param'
        % this('check_param',param)
        out1 = check_param(varargin{2});
        
      case 'simulink_params'
        % this('simulink_params',settings)
        out1 = simulink_params(varargin{2});
        
      case 'simulink_reverse_params'
        % this('simulink_reverse_params',settings)
        out1 = simulink_reverse_params(varargin{2});
        
      % NNET 6.0 Compatibility
      case 'dx', out1 = dy_dx(varargin{2:4});
      case 'pcheck', out1 = check_param(varargin{2});
        
      otherwise,
        try
          out1 = eval(['INFO.' in1]);
        catch me %#ok<NASGU>
          nnerr.throw(['Unrecognized first argument: ''' in1 ''''])
        end
    end
    return
  end
  [args,param] = nnparam.extract_param(varargin,INFO.defaultParam);
  [x,ii,jj,wasCell] = nncell2mat(args{1});
  if length(args) > 1
    % NNET 6.0 Compatibility
    fn = fieldnames(INFO.defaultParam);
    if length(args)-1 == length(fn)
      for i=1:length(fn)
        param.(fn{i}) = args{i+1};
      end
    end
  end
  [out1,out2] = create(x,param);
  if (wasCell), out1 = mat2cell(out1,ii,jj); end
end

function d = dy_dx_num(x,y,settings)
  delta = 1e-7;
  [N,Q] = size(x);
  M = size(y,1);
  d = cell(1,Q);
  for q=1:Q
    dq = zeros(M,N);
    xq = x(:,q);
    for i=1:N
      y1 = apply(addx(xq,i,-2*delta),settings);
      y2 = apply(addx(xq,i,-delta),settings);
      y3 = apply(addx(xq,i,+delta),settings);
      y4 = apply(addx(xq,i,+2*delta),settings);
      dq(:,i) = (y1 - 8*y2 + 8*y3 - y4) / (12*delta);
    end
    d{q} = dq;
  end
end

function d = dx_dy_num(x,y,settings)
  delta = 1e-7;
  [N,Q] = size(x);
  M = size(y,1);
  d = cell(1,Q);
  M = size(y,1);
  for q=1:Q
    dq = zeros(N,M);
    yq = y(:,q);
    for i=1:M
      x1 = reverse(addx(yq,i,-2*delta),settings);
      x2 = reverse(addx(yq,i,-delta),settings);
      x3 = reverse(addx(yq,i,+delta),settings);
      x4 = reverse(addx(yq,i,+2*delta),settings);
      dq(:,i) = (x1 - 8*x2 + 8*x3 - x4) / (12*delta);
    end
    dq(~isfinite(dq)) = 0;
    d{q} = dq;
  end
end

function x = addx(x,i,v)
  x(i) = x(i) + v;
end

function sf = subfunctions
  sf.create = @create;
  sf.apply = @apply;
  sf.reverse = @reverse;
  sf.dy_dx = @dy_dx;
  sf.dx_dy = @dx_dy;
  sf.dy_dx_num = @dy_dx_num;
  sf.dx_dy_num = @dx_dy_num;
end

function info = get_info
  info = nnfcnProcessing(mfilename,function_name,7,subfunctions,...
    process_inputs,process_outputs,is_continuous,parameters);
end

%  BOILERPLATE_END
%% =======================================================

function name = function_name, name = 'Remove Selected Rows'; end
function flag = process_inputs, flag = true; end
function flag = process_outputs, flag = false; end
function flag = is_continuous, flag = true; end

function param = parameters
  param = nnetParamInfo('ind','Rows to Remove','nntype.index_row',[],...
    'Indices of rows to remove.');
end

function err = check_param(param)
 err = '';
end

function [y,settings] = create(x,param)
  R = size(x,1);
  if  any(param.ind > R)
    error(message('nnet:NNData:XRowIndexTooLarge'));
  end  
  % Reduce the transformation matrix appropriately
  settings.name = 'removerows';
  settings.xrows = R;
  settings.yrows = R-length(param.ind);
  settings.remove_ind = param.ind;
  settings.keep_ind = 1:R;
  settings.keep_ind(settings.remove_ind) = [];
  settings.no_change = isempty(settings.remove_ind);
  y = apply(x,settings);
end

function y = apply(x,settings)
  y = x(settings.keep_ind,:);
end

function x = reverse(y,settings)
  Q = size(y,2);
  x = zeros(settings.xrows,Q);
  x(settings.keep_ind,:) = y;
  x(settings.remove_ind,:) = NaN; % Don't cares
end

function d = dy_dx(x,y,settings)
  Q = size(x,2);
  dq = zeros(settings.yrows,settings.xrows);
  for i=1:length(settings.keep_ind)
    dq(i,settings.keep_ind(i)) = 1;
  end
  d = cell(1,Q);
  d(:) = {dq};
end

function d = dx_dy(x,y,settings)
  Q = size(x,2);
  d = cell(1,Q);
  d(:) = {dy_dx(x,y,settings)'};
end

function p = simulink_params(settings)
  p = { ...
    'inputSize',mat2str(settings.xrows);
    'keep',mat2str(settings.keep_ind);
    };
end

function p = simulink_reverse_params(settings)
  recreate = zeros(1,settings.xrows);
  recreate(settings.keep_ind) = (1:settings.yrows);
  p = { ...
    'inputSize',mat2str(settings.xrows);
    'rearrange',mat2str(recreate);
    };
end
