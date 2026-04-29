import React, { useState, useEffect } from 'react'
import {
  Card,
  DatePicker,
  Button,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  message,
  Space,
  Divider,
  Alert,
} from 'antd'
import {
  BarChartOutlined,
  SyncOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons'
import { reportApi } from '../api'
import dayjs from 'dayjs'

const { RangePicker } = DatePicker

function Reports() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(dayjs())
  const [currentReport, setCurrentReport] = useState(null)

  const loadReports = async () => {
    setLoading(true)
    try {
      const data = await reportApi.list()
      setReports(data || [])
    } catch (error) {
      console.error('加载报表列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadReportByDate = async (date) => {
    setLoading(true)
    try {
      const data = await reportApi.getByDate(date.format('YYYY-MM-DD'))
      setCurrentReport(data)
    } catch (error) {
      message.error('加载报表失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const generateReport = async (date) => {
    try {
      const data = await reportApi.generate(date.format('YYYY-MM-DD'))
      setCurrentReport(data)
      message.success('报表生成成功')
      loadReports()
    } catch (error) {
      message.error('生成报表失败: ' + (error.message || '未知错误'))
    }
  }

  useEffect(() => {
    loadReports()
    loadReportByDate(dayjs())
  }, [])

  const handleDateChange = (date) => {
    setSelectedDate(date)
    loadReportByDate(date)
  }

  const itemColumns = [
    {
      title: '事项名称',
      dataIndex: 'itemName',
      key: 'itemName',
    },
    {
      title: '总办件数',
      dataIndex: 'totalCount',
      key: 'totalCount',
      render: (v) => <Tag color="blue">{v}</Tag>,
    },
    {
      title: '已办结',
      dataIndex: 'completedCount',
      key: 'completedCount',
      render: (v) => <Tag color="success">{v}</Tag>,
    },
    {
      title: '超期数',
      dataIndex: 'overdueCount',
      key: 'overdueCount',
      render: (v) => <Tag color="error">{v}</Tag>,
    },
    {
      title: '退回数',
      dataIndex: 'returnedCount',
      key: 'returnedCount',
      render: (v) => <Tag color="warning">{v}</Tag>,
    },
    {
      title: '超期率',
      dataIndex: 'overdueRate',
      key: 'overdueRate',
      render: (v) => (
        <Tag color={v > 10 ? 'error' : v > 5 ? 'warning' : 'success'}>
          {v}%
        </Tag>
      ),
    },
    {
      title: '退回率',
      dataIndex: 'returnRate',
      key: 'returnRate',
      render: (v) => (
        <Tag color={v > 10 ? 'error' : v > 5 ? 'warning' : 'success'}>
          {v}%
        </Tag>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" style={{ marginBottom: 24 }}>
        <Col>
          <h2 style={{ margin: 0 }}>统计报表</h2>
        </Col>
        <Col>
          <Space>
            <DatePicker
              value={selectedDate}
              onChange={handleDateChange}
              style={{ width: 200 }}
            />
            <Button
              icon={<SyncOutlined />}
              onClick={() => generateReport(selectedDate)}
            >
              生成当日报表
            </Button>
            <Button icon={<SyncOutlined />} onClick={loadReports}>
              刷新
            </Button>
          </Space>
        </Col>
      </Row>

      {currentReport && (
        <>
          <Divider>
            <BarChartOutlined style={{ marginRight: 8 }} />
            {dayjs(currentReport.reportDate).format('YYYY年MM月DD日')} 报表
          </Divider>

          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={4}>
              <Card>
                <Statistic
                  title="总办件数"
                  value={currentReport.totalCases}
                  prefix={<FileTextOutlined />}
                />
              </Card>
            </Col>
            <Col span={4}>
              <Card>
                <Statistic
                  title="已受理"
                  value={currentReport.acceptedCases}
                  valueStyle={{ color: '#1890ff' }}
                  prefix={<ClockCircleOutlined />}
                />
              </Card>
            </Col>
            <Col span={4}>
              <Card>
                <Statistic
                  title="已办结"
                  value={currentReport.completedCases}
                  valueStyle={{ color: '#52c41a' }}
                  prefix={<CheckCircleOutlined />}
                />
              </Card>
            </Col>
            <Col span={4}>
              <Card>
                <Statistic
                  title="超期办件"
                  value={currentReport.overdueCases}
                  valueStyle={{ color: '#ff4d4f' }}
                  prefix={<WarningOutlined />}
                />
              </Card>
            </Col>
            <Col span={4}>
              <Card>
                <Statistic
                  title="退回办件"
                  value={currentReport.returnedCases}
                  valueStyle={{ color: '#fa8c16' }}
                  prefix={<CloseCircleOutlined />}
                />
              </Card>
            </Col>
            <Col span={4}>
              <Card>
                <Statistic
                  title="已撤回"
                  value={currentReport.withdrawnCases}
                  prefix={<CloseCircleOutlined />}
                />
              </Card>
            </Col>
          </Row>

          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={12}>
              <Card title="超期率">
                <Statistic
                  value={currentReport.overdueRate}
                  suffix="%"
                  valueStyle={{
                    color:
                      currentReport.overdueRate > 10
                        ? '#ff4d4f'
                        : currentReport.overdueRate > 5
                        ? '#fa8c16'
                        : '#52c41a',
                  }}
                />
              </Card>
            </Col>
            <Col span={12}>
              <Card title="退回率">
                <Statistic
                  value={currentReport.returnRate}
                  suffix="%"
                  valueStyle={{
                    color:
                      currentReport.returnRate > 10
                        ? '#ff4d4f'
                        : currentReport.returnRate > 5
                        ? '#fa8c16'
                        : '#52c41a',
                  }}
                />
              </Card>
            </Col>
          </Row>

          {currentReport.itemReports && currentReport.itemReports.length > 0 && (
            <Card title="各事项统计">
              <Table
                columns={itemColumns}
                dataSource={currentReport.itemReports}
                rowKey="itemId"
                pagination={false}
              />
            </Card>
          )}
        </>
      )}

      {!currentReport && (
        <Alert
          message="暂无报表数据"
          description="点击'生成当日报表'按钮生成报表"
          type="info"
          showIcon
        />
      )}

      {reports.length > 0 && (
        <div style={{ marginTop: 24 }}>
          <Divider>历史报表</Divider>
          <Table
            columns={[
              {
                title: '报表日期',
                dataIndex: 'reportDate',
                key: 'reportDate',
                render: (date) => dayjs(date).format('YYYY年MM月DD日'),
              },
              {
                title: '总办件数',
                dataIndex: 'totalCases',
                key: 'totalCases',
                render: (v) => <Tag color="blue">{v}</Tag>,
              },
              {
                title: '已办结',
                dataIndex: 'completedCases',
                key: 'completedCases',
                render: (v) => <Tag color="success">{v}</Tag>,
              },
              {
                title: '超期数',
                dataIndex: 'overdueCases',
                key: 'overdueCases',
                render: (v) => <Tag color="error">{v}</Tag>,
              },
              {
                title: '超期率',
                dataIndex: 'overdueRate',
                key: 'overdueRate',
                render: (v) => `${v}%`,
              },
              {
                title: '退回率',
                dataIndex: 'returnRate',
                key: 'returnRate',
                render: (v) => `${v}%`,
              },
              {
                title: '生成时间',
                dataIndex: 'createdAt',
                key: 'createdAt',
                render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'),
              },
            ]}
            dataSource={reports}
            rowKey="id"
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      )}
    </div>
  )
}

export default Reports
