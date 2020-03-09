/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Condition expression
 */

angular.module('flowableModeler').controller('FlowableConditionExpressionCtrl', [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        backdrop: 'static',
        keyboard: false,
        template: 'editor-app/configuration/properties/condition-expression-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('flowableModeler').controller('FlowableConditionExpressionPopupCtrl',
    ['$rootScope', '$http', '$scope', '$translate', 'FormBuilderService', function ($rootScope, $http, $scope, $translate, FormBuilderService) {

        // 标签tab
        $scope.currentIndex = 0;
        // 字段选择
        $scope.flowOptions = [];
        // 符号
        $scope.symbolOptions = [
            // {id:"<",leftLabel:"大于",rightLabel:"小于"},
            {id: "<=", leftLabel: "大于等于", rightLabel: "小于等于"},
        ];
        // 自定义表达式的值
        $scope.personalExpression = {
            leftValue: null,
            leftSymbol: null,
            field: null,
            rightSymbol: null,
            rightValue: null
        };
        // Put json representing assignment on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null
            && $scope.property.value.expression !== undefined
            && $scope.property.value.expression !== null) {

            $scope.expression = $scope.property.value.expression;

        } else if ($scope.property.value !== undefined && $scope.property.value !== null) {
            $scope.expression = {type: 'static', staticValue: $scope.property.value};

        } else {
            $scope.expression = {};
        }

        // 获取字段信息
        $http.get(FLOWABLE.APP_URL.getFlowOption($rootScope.modelData.key))
            .success(function (data, status, headers, config) {
                $scope.flowOptions = data.data;
                // 回显信息
                $scope.flowEcho();
            })
            .error(function (data) {
                console.log('error');
            });


        $scope.flowEcho = function () {
            // 赋值
            if ($scope.expression.staticValue !== null && $scope.expression.staticValue !== '') {
                // 查看表达式里是否含有message 和 同意或驳回
                let result = false;
                if (($scope.expression.staticValue.indexOf('同意') !== -1 || $scope.expression.staticValue.indexOf('驳回') !== -1)
                    && $scope.expression.staticValue.indexOf('message') !== -1) {
                    result = true;
                }
                // 如果是true，则代表是处在自定义tab页面
                $scope.currentIndex = result ? 0 : 1;
                if ($scope.currentIndex === 0) {
                    // 处于同意或者驳回页面
                    if ($scope.expression.staticValue.indexOf('同意') !== -1) {
                        $scope.expression.staticValue = '同意';
                    } else {
                        $scope.expression.staticValue = '驳回';
                    }
                } else {
                    $scope.personalExpression = getParseExpression($scope.expression.staticValue);
                }
            } else {
                // 如果没有值，则当前处在第一个tab
                $scope.currentIndex = 0;
                // 值为无
                $scope.expression.staticValue = '无';
            }
        };

        $scope.save = function () {
            // 拼接线条的名字
            let showFlowName = null;
            if ($scope.currentIndex === 0) {
                if ($scope.expression.staticValue === '无') {
                    $scope.expression.staticValue = '';
                } else {
                    showFlowName = $scope.expression.staticValue;
                    $scope.expression.staticValue = "${message == '" + $scope.expression.staticValue + "'}";
                }
            } else {
                // 自定义验证
                // 1.属性是否选择
                if ($scope.personalExpression.field == null) {
                    alert("请选择需要判断的属性");
                    return;
                }
                if ($scope.personalExpression.leftValue == null && $scope.personalExpression.leftSymbol == null
                    && $scope.personalExpression.rightSymbol == null && $scope.personalExpression.rightValue == null) {
                    alert("请填写完整表达式");
                    return;
                }
                // 2.查看左边部分是否填写
                if (($scope.personalExpression.leftSymbol == null && $scope.personalExpression.leftValue != null) ||
                    ($scope.personalExpression.leftSymbol != null && $scope.personalExpression.leftValue == null)) {
                    alert("请填写完整左边表达式");
                    return;
                }
                // 3.查看右边部分是否填写
                if (($scope.personalExpression.rightSymbol == null && $scope.personalExpression.rightValue != null) ||
                    ($scope.personalExpression.rightSymbol != null && $scope.personalExpression.rightValue == null)) {
                    alert("请填写完整右边表达式");
                    return;
                }
                // 4.查看左右两侧是否为数字
                if (isNaN($scope.personalExpression.leftValue)) {
                    alert("左侧属性值请输入数字");
                    return;
                }
                if (isNaN($scope.personalExpression.rightValue)) {
                    alert("右侧属性值请输入数字");
                    return;
                }
                // 线条属性名称
                const flowObject = $scope.flowOptions.find(f => f.id === $scope.personalExpression.field);
                if (!flowObject) {
                    alert("需要判断的属性不存在");
                    return;
                }
                showFlowName = flowObject.label;
                // 记录最大值和最小值的数组
                const minAndMaxArr = [null, null];
                // 左侧是否存在
                let left = false;
                // 线条表达式
                let expression = "${";
                // 赋值表达式
                if ($scope.personalExpression.leftSymbol) {
                    expression += $scope.personalExpression.leftValue + $scope.personalExpression.leftSymbol;
                    const symbolObject = $scope.symbolOptions.find(s => s.id === $scope.personalExpression.leftSymbol);
                    showFlowName += symbolObject.leftLabel + $scope.personalExpression.leftValue;
                    left = true;
                    minAndMaxArr[0] = parseInt($scope.personalExpression.leftValue);
                }
                expression += $scope.personalExpression.field;
                if ($scope.personalExpression.rightSymbol) {
                    if (left) {
                        expression += ' && ' + $scope.personalExpression.field;
                        showFlowName += '，'
                    }
                    expression += $scope.personalExpression.rightSymbol + $scope.personalExpression.rightValue;
                    const symbolObject = $scope.symbolOptions.find(s => s.id === $scope.personalExpression.rightSymbol);
                    showFlowName += symbolObject.rightLabel + $scope.personalExpression.rightValue;
                    minAndMaxArr[1] = parseInt($scope.personalExpression.rightValue);
                }
                expression += "}";
                $scope.expression.staticValue = expression;
                $scope.expression.personalExpression = $scope.personalExpression;
                $scope.expression.minAndMaxArr = minAndMaxArr;
            }
            $scope.expression.showFlowName = showFlowName;
            $scope.property.value = {expression: $scope.expression};
            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        // Close button handler
        $scope.close = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };


        $scope.selectTab = function (num) {
            $scope.currentIndex = num;
        };

        // 重置属性
        $scope.reset = function () {
            for (const key in $scope.personalExpression) {
                $scope.personalExpression[key] = null;
            }
        }

    }]);