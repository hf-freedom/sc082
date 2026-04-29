import React, { useState, useEffect } from 'react'
import {
  Table,
  Tag,
  Button,
  Card,
  message,
  Space,
  Row,
  Col,
  Statistic,
  Timeline,
  Divider,
} from 'antd'
import {
  WarningOutlined,
  SyncOutlined,
  EyeOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { supervisionApi } from '../api'
import dayjs from 'dayjs'

function OverdueCases() {
  const navigate = useNavigate()
  const [overdueCases, setOverdueCases] = useState([])
  const [supervisionRecords, setSupervisionRecords] = useState([])
  const [loading, setLoading] = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const [cases, records] = await Promise.all([
        supervisionApi.listOverdue(),
        supervisionApi.listRecords(),
      ])
      setOverdueCases(cases || [])
      setSupervisionRecords(records || [])
    } catch (error) {
      message.error('加载数据失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const handleManualCheck = async () => {
    try {
      await supervisionApi.check()
      message.success('督办检查完成')
      loadData()
    } catch (error) {
      message.error('检查失败: ' + (error.message || '未知错误'))
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const getOverdueDays = (dueDate) => {
    if (!dueDate) return 0
    const due = dayjs(dueDate)
    const now = dayjs()
    return now.diff(due, 'day')
  }

  const caseColumns = [
    {
      title: '案件编号',
      dataIndex: 'caseNumber',
      key: 'caseNumber',
      width: 180,
      render: (text, record) => (
        <a onClick={() => navigate(`/cases/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: '事项名称',
      dataIndex: 'itemName',
      key: 'itemName',
      width: 150,
    },
    {
      title: '申请人',
      dataIndex: 'applicantName',
      key: 'applicantName',
      width: 100,
    },
    {
      title: '联系电话',
      dataIndex: 'applicantPhone',
      key: 'applicantPhone',
      width: 120,
    },
    {
      title: '当前状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => <Tag color="error">{status === 'OVERDUE' ? '已超期' : status}</Tag>,
    },
    {
      title: '当前节点',
      dataIndex: 'currentNodeName',
      key: 'currentNodeName',
      width: 100,
      render: (text) => text || '-',
    },
    {
      title: '经办人',
      dataIndex: 'handler',
      key: 'handler',
      width: 100,
      render: (text) => text || '未分配',
    },
    {
      title: '到期时间',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 160,
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '超期天数',
      key: 'overdueDays',
      width: 100,
      render: (_, record) => {
        const days = getOverdueDays(record.dueDate)
        return (
          <Tag color={days > 7 ? 'red' : 'orange'}>
            {days} 天
          </Tag>
        )
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/cases/${record.id}`)}
        >
          查看
        </Button>
      ),
    },
  ]

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="超期办件总数"
              value={overdueCases.length}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="超期7天以上"
              value={overdueCases.filter((c) => getOverdueDays(c.dueDate) > 7).length}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="超期3-7天"
              value={
                overdueCases.filter(
                  (c) => getOverdueDays(c.dueDate) > 3 && getOverdueDays(c.dueDate) <= 7
                ).length
              }
              valueStyle={{ color: '#fa8c16' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="督办记录数"
              value={supervisionRecords.length}
              valueStyle={{ color: '#722ed1' }}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row justify="space-between" style={{ marginBottom: 16 }}>
        <Col>
          <h3 style={{ margin: 0 }}>超期办件列表</h3>
        </Col>
        <Col>
          <Space>
            <Button
              type="primary"
              danger
              icon={<SyncOutlined />}
              onClick={handleManualCheck}
            >
              执行督办检查
            </Button>
            <Button icon={<SyncOutlined />} onClick={loadData}>
              刷新
            </Button>
          </Space>
        </Col>
      </Row>

      <Table
        columns={caseColumns}
        dataSource={overdueCases}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1400 }}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`,
        }}
      />

      {supervisionRecords.length > 0 && (
        <div style={{ marginTop: 24 }}>
          <Divider>督办记录</Divider>
          <Card title="最近督办记录">
            <Timeline>
              {supervisionRecords
                .slice()
                .reverse()
                .slice(0, 10)
                .map((record, index) => (
                  <Timeline.Item key={index} color="red">
                    <p>
                      <strong>案件编号:</strong>{' '}
                      <a onClick={() => navigate(`/cases/${overdueCases.find((c) => c.id === record.caseId)?.id || ''}`)}>
                        {record.caseNumber}
                      </a>
                    </p>
                    <p>
                      <strong>事项名称:</strong> {record.itemName}
                    </p>
                    <p>
                      <strong>申请人:</strong> {record.applicantName}
                    </p>
                    <p>
                      <strong>经办人:</strong> {record.handler || '未分配'}
                    </p>
                    <p>
                      <strong>超期时长:</strong> {record.overdueHours} 小时
                    </p>
                    <p>
                      <strong>督办时间:</strong>{' '}
                      {dayjs(record.supervisionTime).format('YYYY-MM-DD HH:mm:ss')}
                    </p>
                    <p>
                      <strong>状态:</strong> <Tag color="default">{record.status}</Tag>
                    </p>
                  </Timeline.Item>
                ))}
            </Timeline>
          </Card>
        </div>
      )}
    </div>
  )
}

export default OverdueCases
