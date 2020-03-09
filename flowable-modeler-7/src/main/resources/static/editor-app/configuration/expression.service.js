getParseMinAndMax = function (staticValue) {
    const personalExpression = this.getParseExpression(staticValue);
    // 记录最大值和最小值的数组
    const minAndMaxArr = [null, null];
    // 赋值表达式
    if (personalExpression.leftSymbol) {
        minAndMaxArr[0] = parseInt(personalExpression.leftValue);
    }
    if (personalExpression.rightSymbol) {
        minAndMaxArr[1] = parseInt(personalExpression.rightValue);
    }
    return minAndMaxArr;
};
// 根据流条件得到表达式对象
getParseExpression = function (staticValue) {
    // 自定义表达式的值
    const personalExpression = {
        leftValue: null,
        leftSymbol: null,
        field: null,
        rightSymbol: null,
        rightValue: null
    };
    // 去除{前面
    const string1 = staticValue.substring(staticValue.indexOf("{") + 1);
    // 去除}后面
    const expression = string1.substring(0, string1.indexOf("}"));
    // 判断是否存在 && 符号
    if (expression.indexOf("&&") !== -1) {
        // 存在 && 符号 类似 1<hour && hour<5
        const expressionArr = expression.split("&&");
        const expressionSplitLeft = this.expressionSplit(expressionArr[0]);
        this.setPersonalExpression(expressionSplitLeft, personalExpression);
        const expressionSplitRight = this.expressionSplit(expressionArr[1]);
        this.setPersonalExpression(expressionSplitRight, personalExpression);
    } else {
        // 只存在一个表达式
        const expressionSplit = this.expressionSplit(expression);
        this.setPersonalExpression(expressionSplit, personalExpression);
    }
    return personalExpression;
};

// 分割表达式
expressionSplit = function (expression) {
    const reg = /(\w+)([<=]+)(\w+)/;
    const result = expression.split(reg)
        .filter(s => s.trim() !== '');
    return result;
};

// 设置表达式的值 类似 hour<5 或者 5<hour
setPersonalExpression = function (expressionSplit, personalExpression) {
    if (isNaN(expressionSplit[0])) {
        // 第一个值不是数字 hour<5
        personalExpression.field = expressionSplit[0];
        // 标志符号
        personalExpression.rightSymbol = expressionSplit[1];
        // 值
        personalExpression.rightValue = expressionSplit[2];
    } else {
        // 第一个值是数字 5<hour
        personalExpression.leftValue = expressionSplit[0];
        personalExpression.leftSymbol = expressionSplit[1];
        personalExpression.field = expressionSplit[2];
    }
};

// angular.module('flowableModeler')
//
//     .factory('utilService', function() {
//         return {
//
//
//         }
//     });



